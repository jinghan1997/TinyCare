import time, os
import pyrebase

from datetime import datetime
import RPi.GPIO as GPIO
from picamera import PiCamera
from hx711 import HX711

SYS_ID = "id1000001" # change this to be custom-set based on sth else
INIT_DATA = {
	"type": "hamster",
	"autoTopUpSwitch": "false",
	"foodAmt": "empty",
	"picUrl": "",
	"prevFoodTopUpDateTime": "",
	"prevWaterTopUpDateTime": "",
	"takePic": "false",
	"topUpFood": "false",
	"topUpWater": "false",
	"waterAmt": "empty",
	"pictureToggle": 0
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

control_pins = [7, 11, 13, 15]
valve_pins = 19
weight_sensor_pins = [29, 31]

hx = HX711(weight_sensor[0], weight_sensor[1])
GPIO.setup(valve_pins, GPIO.OUT)
hx.set_reading_format("MSB", "MSB")
hx.set_reference_unit_A(2800)
hx.set_reference_unit_B(1450)
hx.reset()
# to use both channels, you'll need to tare them both
hx.tare_A()
hx.tare_B()
time.sleep(0.1)


# initialise camera
camera = PiCamera()
camera.rotation = 180
# let camera warm up
time.sleep(0.1)

config = {
	"apiKey": "AIzaSyAUE2dzD4BuQehj0XW5SHANCIvjj4yMmKc",
	"authDomain": "tinycare-b19ba.firebaseapp.com",
	"databaseURL": "https://tinycare-b19ba.firebaseio.com/",
	"storageBucket": "tinycare-b19ba.appspot.com"
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

print(db.child(SYS_ID).get().val())

food_empty_time = None
water_empty_time = None
max_hr_to_feed = 3
failcheck_max_hr = 6
save_time = None
water_off_time = 10

try:
	while True:
		# get firebase vars
		data = db.child(SYS_ID).get().val()
		if data is None:
			db.child(SYS_ID).set(INIT_DATA)
		else:
			if data["foodAmt"] == "empty":
				food_empty_time = datetime.now()
			if data["waterAmt"] == "empty":
				water_empty_time = datetime.now()

			if data["autoTopUpSwitch"] == "true":
				if (food_empty_time and (datetime.now() - food_empty_time).seconds > hoursToSeconds(max_hr_to_feed)) and data["foodAmt"] == "empty":
					db.child(SYS_ID).child("topUpFood").set("true")
				if (water_empty_time and (now - water_empty_time).seconds > hoursToSeconds(max_hr_to_feed)) and data["waterAmt"] == "empty":
					db.child(SYS_ID).child("topUpWater").set("true")
			else:
				# failcheck
				if (food_empty_time and (datetime.now() - food_empty_time).seconds > hoursToSeconds(failcheck_max_hr)) and data["foodAmt"] == "empty":
					db.child(SYS_ID).child("topUpFood").set("true")
				if (water_empty_time and (now - water_empty_time).seconds > hoursToSeconds(failcheck_max_hr)) and data["waterAmt"] == "empty":
					db.child(SYS_ID).child("topUpWater").set("true")

			if data["topUpFood"] == "true":
				print("topping up food")
				turnMotor()
				db.child(SYS_ID).child("topUpFood").set("false")
				db.child(SYS_ID).child("prevFoodTopUpDateTime").set(datetime.now().strftime("%d %b %Y, %I:%M%p"))

			if data["topUpWater"] == "true":
				print("topping up water")
				saveTime = datetime.now()
				GPIO.output(valve_pins, GPIO.HIGH)
				db.child(SYS_ID).child("topUpWater").set("false")
				db.child(SYS_ID).child("prevWaterTopUpDateTime").set(datetime.now().strftime("%d %b %Y, %I:%M%p"))

			if data["takePic"] == "true":
				print("taking picture")
				im = "./temp.jpg"
				camera.capture(im)

				storage.child(SYS_ID).child(str(data["pictureToggle"])).put(im)
				im_url = storage.child(SYS_ID).child(str(data["pictureToggle"])).get_url(None)
				print("url: " + im_url)
				db.child(SYS_ID).child("pictureToggle").set((data["pictureToggle"] + 1) % 2)
				db.child(SYS_ID).child("picUrl").set(im_url)

				db.child(SYS_ID).child("takePic").set("false")

		# add the sensor checks
		# weight checks
		val_A = hx.get_weight_A(5)
		val_B = hx.get_weight_B(5)
		print ("A: {0}  B:{1}".format( val_A, val_B ))

		if val_B < 2:
			db.child(SYS_ID).child("foodAmt").set("empty")
		elif val_B < 15:
			db.child(SYS_ID).child("foodAmt").set("low")
		elif val_B < 32:
			db.child(SYS_ID).child("foodAmt").set("mid")
		else:
			db.child(SYS_ID).child("foodAmt").set("full")

		if val_A < 2:
			db.child(SYS_ID).child("waterAmt").set("empty")
		elif val_A < 25:
			db.child(SYS_ID).child("waterAmt").set("low")
		elif val_A < 50:
			db.child(SYS_ID).child("waterAmt").set("mid")
		else:
			db.child(SYS_ID).child("waterAmt").set("full")

		hx.power_down()
		hx.power_up()


		# turn off water
		if (saveTime is not None and (datetime.now()-saveTime).seconds > water_off_time):
			GPIO.output(valve_pins, GPIO.LOW)

		time.sleep(0.1)

except KeyboardInterrupt:
	print("exited using ctrl-c")
except Exception as e:
	print(e)

GPIO.cleanup()