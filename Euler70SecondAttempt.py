#!/usr/bin/env python3
import matplotlib.pyplot as plt
import numpy as np
import time
time.process_time()
time.perf_counter()

#--------CALCULATE PRIMES--------------------------------------

calculate_through = 10000001
calculate_through_prime = 10310000
	
iscomposite = [False] * calculate_through_prime
primes = []
iscomposite[0] =  True
iscomposite[1] = True
factors = []

for x in range(2, calculate_through_prime):
	if iscomposite[x]:
		continue
	else:
		primes.append(x)
		for y in range(x*x, calculate_through_prime, x):
			iscomposite[y] = True


#-----------EULER 70 CODE---------------------------------------

#fig, ax = plt.subplots()
#ax.plot(primes, range(1, len(primes) + 1), label="primes")
#ax.plot([2,9999991],[20000,684579],label="estimation")
#plt.show()

#Calculate Psi	
def calculate_psi(x):
	psi = x
	#Get Factors
	factors_left = x
	factors = []
	
	for y in range(int(x*0.06645797)+20000): #(x*0.06645797) + 20000 is an upper bound to the number of primes that exist from 2 to x, (Designed for numbers up to 10,000,000)
		if x % primes[y] == 0:
			while factors_left % primes[y] == 0:
				factors_left = factors_left // primes[y]
			factors.append(primes[y])
			if factors_left == 1:
				break
			
	#Calculate Psi From Factors
	for y in range(len(factors)):
		psi = psi - (x//factors[y])
	psi = psi + len(factors) - 1
	
	
	if len(factors) >= 2:
		#Calculate Dups
		dups = 0
		for y in range(len(factors)-1):
			for z in range(y+1,len(factors)):
				multiple = factors[y] * factors[z]
				dups = dups + (x // multiple) - 1
		psi = psi + dups
	else:
		return psi, factors
	

	if len(factors) >= 3:
		#calculate Trips
		trips = 0
		for y in range(len(factors)-2):
			for z in range(y+1,len(factors)-1):
				for a in range(z+1,len(factors)):
					multiple = factors[a]*factors[y]*factors[z]
					trips = trips + (x // multiple) - 1
		psi = psi - trips
	else:
		return psi, factors
	

	if len(factors) >= 4:
		#Calculate Quads
		quads = 0
		for y in range(len(factors)-3):
			for z in range(y+1,len(factors)-2):
				for a in range(z+1,len(factors)-1):
					for b in range(a+1,len(factors)):
						multiple = factors[b]*factors[a]*factors[y]*factors[z]
						quads = quads + (x // multiple) - 1
		psi = psi + quads
	else:
		return psi, factors


	if len(factors) >= 5:
		#Calculate Quints
		quints = 0
		for y in range(len(factors)-4):
			for z in range(y+1,len(factors)-3):
				for a in range(z+1,len(factors)-2):
					for b in range(a+1,len(factors)-1):
						for c in range(b+1,len(factors)):
							multiple = factors[c]*factors[b]*factors[a]*factors[y]*factors[z]
							quads = quads + (x // multiple) - 1
		psi = psi - quints
	else:
		return psi, factors		
	

	if len(factors) >= 6:		
		#Calculate Sexts
		sexts = 0
		for y in range(len(factors)-5):
			for z in range(y+1,len(factors)-4):
				for a in range(z+1,len(factors)-3):
					for b in range(a+1,len(factors)-2):
						for c in range(b+1,len(factors)-1):
							for d in range(c+1, len(factors)):
								multiple = factors[d]*factors[c]*factors[b]*factors[a]*factors[y]*factors[z]
								sexts = sexts + (x // multiple) - 1
		psi = psi + sexts
	else:
		return psi, factors
	

	if len(factors) >= 7:
		#Calculate Septs
		septs = 0
		for y in range(len(factors)-6):
			for z in range(y+1,len(factors)-5):
				for a in range(z+1,len(factors)-4):
					for b in range(a+1,len(factors)-3):
						for c in range(b+1,len(factors)-2):
							for d in range(c+1, len(factors)-1):
								for e in range(d+1, len(factors)):
									multiple = factors[e]*factors[d]*factors[c]*factors[b]*factors[a]*factors[y]*factors[z]
									septs = septs + (x // multiple) - 1
		psi = psi - septs
	else:
		return psi, factors

	
	if len(factors) >= 8:
		#Calculate Octs
		octs = 0
		for y in range(len(factors)-7):
			for z in range(y+1,len(factors)-6):
				for a in range(z+1,len(factors)-5):
					for b in range(a+1,len(factors)-4):
						for c in range(b+1,len(factors)-3):
							for d in range(c+1, len(factors)-2):
								for e in range(d+1, len(factors)-1):
									for f in range(e+1,len(factors)):
										multiple = factors[f]*factors[e]*factors[d]*factors[c]*factors[b]*factors[a]*factors[y]*factors[z]
										octs = octs + (x // multiple) - 1
		psi = psi + octs
	else:
		return psi, factors



	if len(factors) >= 9:
		#Calculate Nons
		nons = 0
		for y in range(len(factors)-8):
			for z in range(y+1,len(factors)-7):
				for a in range(z+1,len(factors)-6):
					for b in range(a+1,len(factors)-5):
						for c in range(b+1,len(factors)-4):
							for d in range(c+1, len(factors)-3):
								for e in range(d+1, len(factors)-2):
									for f in range(e+1,len(factors)-1):
										for g in range(f+1,len(factors)):
											multiple = factors[g]*factors[f]*factors[e]*factors[d]*factors[c]*factors[b]*factors[a]*factors[y]*factors[z]
											nons = nons + (x // multiple) - 1
		psi = psi - nons
	else:
		return psi, factors
	return psi, factors


#----------Permuation Function--------------------------------------------------------------------



def are_permutations(num1, num2):
	str1 = str(num1)
	str2 = str(num2)
	is_digit_used = []
	if len(str1) == len(str2):
		for y in range(len(str1)):
			is_digit_used.append(False)
		for y in range(len(str1)):
			for z in range(len(str1)):
				if str1[y] == str2[z] and is_digit_used[z] != True:
					is_digit_used[z] = True
					break
		for y in range(len(str1)):
			if is_digit_used[y] == False:
				return False
		return True
	else:
		return False


#-------PROGRAM CODE-------------------------------------------------------------------------------------------
least_num = 1
least_psi = 1
least_ratio = 5	
for num in range(2,calculate_through):
	
	psi, factors = calculate_psi(num)
	
	if are_permutations(num, psi):
		#print("Num: " + str(num) + "	Psi: " + str(psi) + "	Factors: " + str(factors))
		if num / psi < least_ratio:
			least_ratio = num / psi
			least_num = num
			least_psi = psi
			print("The least ratio is: " + str(least_ratio) + "	The num is: " + str(least_num) + "	The psi is: " + str(least_psi) + "	Time: " + str(time.process_time()) + " seconds")
		else:
			print("Num: " + str(num) + "	Psi: " + str(psi) + "	Factors: " + str(factors) + "	Num w/ least ratio so far: " + str(least_num) + "	Time: " + str(time.process_time()) + " seconds")
	#if num % 100 == 0:
	#	print("Num: " + str(num) + "	Psi: " + str(psi))

print("The least ratio is: " + str(least_ratio) + "	The num is: " + str(least_num) + "	The psi is: " + str(least_psi) + "	Time: " + str(time.process_time()) + " seconds")