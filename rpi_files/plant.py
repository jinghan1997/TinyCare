import time, os, sys
import pyrebase
import threading

from datetime import datetime

import RPi.GPIO as GPIO
from picamera import PiCamera

SYS_ID = "id1000003" # change this to be custom-set based on sth else
INIT_DATA = {
	"type": "plant",
	"waterLow": "false",
	"takePic": "false",
	"picUrl": "",
	"topUpWater": "false",
	"autoTopUpSwitch": "false",
	"prevWaterTopUpDateTime": "",
	"pictureToggle": 0,
}

def hoursToSeconds(hr):
	return hr * 60 * 60

def moistureCallback(pin):
	if GPIO.input(pin):
		print("no water")
		db.child(SYS_ID).child("waterLow").set("true")
	else:
		print("water")
		db.child(SYS_ID).child("waterLow").set("false")

def addWater(pump_pin = 11):
	GPIO.output(pump_pin, GPIO.HIGH)
	time.sleep(10)
	GPIO.output(pump_pin, GPIO.LOW)

moisture_pin = 7
pump_pin = 11

# initialise camera
camera = PiCamera()
camera.rotation = 0
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

last_water = None
max_hr_to_water = 6
failcheck_max_hr = 12
within_hours = 20

try:
	while True:

		# get firebase vars
		data = db.child(SYS_ID).get().val()
		if data is None:
			db.child(SYS_ID).set(INIT_DATA)
		else:

			if data["autoTopUpSwitch"] == "true":
				# auto-topup
				if last_water is None or (datetime.now() - last_water).seconds > hoursToSeconds(max_hr_to_water):
					db.child(SYS_ID).child("topUpWater").set("true")
			else:
				# add a failcheck to ensure a max time spacing of K hours
				if last_water and (datetime.now() - last_water).seconds > hoursToSeconds(failcheck_max_hr):
					db.child(SYS_ID).child("topUpWater").set("true")

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

			if data["topUpWater"] == "true":
				print("topping up water")
				last_water = datetime.now()
				# here is where you will add code to add water
				x = threading.Thread(target = addWater)
				x.start()
				db.child(SYS_ID).child("topUpWater").set("false")
				db.child(SYS_ID).child("prevWaterTopUpDateTime").set(datetime.now().strftime("%d %b %Y, %I:%M%p"))

		# do neccessary checks and update the database
		# moisture => done through callbacks

		time.sleep(0.1)
except KeyboardInterrupt:
	print("exited using ctrl-c")
except Exception as e:
	print(e)

while len(threading.enumerate()) > 1:
	time.sleep(0.1)

GPIO.cleanup()