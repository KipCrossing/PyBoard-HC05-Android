# PyBoard-HC05-Android
Micropython code for the HC05 Bluetooth adaptor and an example application for android devices made specifically for the HC05.


##  main.py


'''
import pyb

blue_uart = pyb.UART(2, 9600)
blue_uart.init(9600, bits=8, stop=1, parity=None)

while True:
    if blue_uart.any():
        line = blue_uart.readline()
        line = str(line,'utf-8')
        if line[-5:-1] == "BTM-":
            if line[-5:] == "BTM-U":
                print("UP")
                blue_uart.write("GO UP")
                

            elif line[-5:] == "BTM-D":
                print("DOWN")
                blue_uart.write("GO DOWN")


            elif line[-5:] == "BTM-L":
                print("LEFT")
                blue_uart.write("GO LEFT")


            elif line[-5:] == "BTM-R":
                print("RIGHT")
                blue_uart.write("GO RIGHT")


        else:
            print(line)
            blue_uart.write("You sent: " + line)

'''

