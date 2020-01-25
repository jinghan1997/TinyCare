import time, os, sys
import pyrebase
import threading

from datetime import datetime

import RPi.GPIO as GPIO
from picamera import PiCamera
import smbus

SYS_ID = "id1000002" # change this to be custom-set based on sth else
INIT_DATA = {
	"type": "fish",
	"waterDirty": "false",
	"waterLow": "false",
	"takePic": "false",
	"picUrl": "",
	"topUpFood": "false",
	"topUpWater": "false",
	"autoTopUpSwitch": "false",
	"prevFoodTopUpDateTime": "",
	"pictureToggle": 0,
	"withinHours": "false"
}

def hoursToSeconds(hr):
	return hr * 60 * 60

def turnMotor(rotations = 512, control_pins = [7, 11, 13, 15]):
	for pin in control_pins:
		GPIO.setup(pin, GPIO.OUT)
		GPIO.output(pin, 0)

	halfstep_seq = [
		[1,0,0,0],
		[1,1,0,0],
		[0,1,0,0],
		[0,1,1,0],
		[0,0,1,0],
		[0,0,1,1],
		[0,0,0,1],
		[1,0,0,1]]

	for i in range(rotations):
		for halfstep in range(8):
			for pin in range(4):
				GPIO.output(control_pins[pin], halfstep_seq[halfstep][pin])
				time.sleep(0.001)

def moistureCallback(pin):
	if GPIO.input(pin):
		print("no water")
		db.child(SYS_ID).child("waterLow").set("true")
	else:
		print("water")
		db.child(SYS_ID).child("waterLow").set("false")

def addWater(pump_pin = 40):
	GPIO.setup(pump_pin, GPIO.OUT)
	GPIO.output(pump_pin, GPIO.HIGH)
	time.sleep(10)
	GPIO.output(pump_pin, GPIO.LOW)

control_pins = [7, 11, 13, 15]
moisture_pin = 12
pump_pin = 40
turbidity_addr_0 = 0x48
turbidity_addr_1 = 0x40

# initialise camera
camera = PiCamera()
camera.rotation = 180
# let camera warm up
time.sleep(0.1)

config = {
	"apiKey": "AIzaSyAUE2dzD4BuQehj0XW5SHANCIvjj4yMmKc",
	"authDomain": "tinycare-b19ba.firebaseapp.com",
	"databaseURL": "https://tinycare-b19ba.firebaseio.com/",
	"storageBucket": "tinycare-b19ba.appspot.com",
	# "serviceAccount": "firebase_key/key.json"
}

try:
	firebase = pyrebase.initialize_app(config)
	db = firebase.database()
	storage = firebase.storage()
	print("firebase connection successful")
	# set up the basic vars if it's not already in the database
	if db.child(SYS_ID).get().val() is None:
		db.child(SYS_ID).set(INIT_DATA)

except:
	print("unable to connect to database.")
	print("an error occurred and the program has stopped.")
	exit()

GPIO.setmode(GPIO.BOARD)

GPIO.setup(moisture_pin, GPIO.IN)
GPIO.setup(pump_pin, GPIO.OUT)

GPIO.add_event_detect(moisture_pin, GPIO.BOTH)
GPIO.add_event_callback(moisture_pin, moistureCallback)

bus = smbus.SMBus(1)

last_feed = None
max_hr_to_feed = 6
failcheck_max_hr = 12
within_hours = 20

turbidity_low = 120
turbidity_high = 120

try:
	while True:

		# get firebase vars
		data = db.child(SYS_ID).get().val()
		if data is None:
			db.child(SYS_ID).set(INIT_DATA)
		else:

			if data["autoTopUpSwitch"] == "true":
				# auto-topup
				if last_feed is None or (datetime.now() - last_feed).seconds > hoursToSeconds(max_hr_to_feed):
					db.child(SYS_ID).child("topUpFood").set("true")
			else:
				# add a failcheck to ensure a max time spacing of K hours
				if last_feed and (datetime.now() - last_feed).seconds > hoursToSeconds(failcheck_max_hr):
					db.child(SYS_ID).child("topUpFood").set("true")

			if data["takePic"] == "true":
				print("taking picture")

				im = "./temp.jpg"
				camera.capture(im)

				# im = "img/hqdefault.jpg"
				storage.child(SYS_ID).child(str(data["pictureToggle"])).put(im)
				im_url = storage.child(SYS_ID).child(str(data["pictureToggle"])).get_url(None)
				print("url: " + im_url)
				db.child(SYS_ID).child("pictureToggle").set((data["pictureToggle"] + 1) % 2)
				db.child(SYS_ID).child("picUrl").set(im_url)
				# picture_toggle = (picture_toggle + 1) % 2


				db.child(SYS_ID).child("takePic").set("false")
				# break

			if data["topUpFood"] == "true":
				print("topping up food")
				last_feed = datetime.now()
				turnMotor(rotations = 128)
				# here is where you will probably call turnMotor()
				db.child(SYS_ID).child("topUpFood").set("false")
				db.child(SYS_ID).child("prevFoodTopUpDateTime").set(datetime.now().strftime("%d %b %Y, %I:%M%p"))

			if data["topUpWater"] == "true":
				print("topping up water")
				# here is where you will add code to add water
				x = threading.Thread(target = addWater)
				x.start()
				db.child(SYS_ID).child("topUpWater").set("false")

			# update withinHours
			if data["prevFoodTopUpDateTime"]:
				prev_time = datetime.strptime(data["prevFoodTopUpDateTime"], "%d %b %Y, %I:%M%p")
				cur_time = datetime.now()
				if data["withinHours"] == "false" and (cur_time - prev_time).seconds < hoursToSeconds(within_hours):
					db.child(SYS_ID).child("withinHours").set("true")
				elif data["withinHours"] == "true" and (cur_time - prev_time).seconds > hoursToSeconds(within_hours):
					db.child(SYS_ID).child("withinHours").set("false")

		# do neccessary checks and update the database
		# moisture => done through callbacks
		# turbidity => here

		bus.write_byte(turbidity_addr_0, turbidity_addr_1)
		turbidity_value = bus.read_byte(turbidity_addr_0)

		# put checks for turbidity here
		if turbidity_value < turbidity_high:
			if data["waterDirty"] == "false": print("water dirty")
			db.child(SYS_ID).child("waterDirty").set("true")
		else:
			if data["waterDirty"] == "true": print("water clean")
			db.child(SYS_ID).child("waterDirty").set("false")

		time.sleep(0.1)
except KeyboardInterrupt:
	print("exited using ctrl-c")
except Exception as e:
	print(e)

while len(threading.enumerate()) > 1:
	time.sleep(0.1)

GPIO.cleanup()