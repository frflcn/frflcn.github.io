#!/usr/bin/env python3

import time

is_digit_used = []
psi = 0

list_of_permutations = []

def find_psi(num):
	
	num_of_relative_primes = 1

	for y in range(2,num):
		list_of_divisors = []
		is_relatively_prime = True
		if num % y == 0:
			is_relatively_prime = False
		else:
			for z in range(2,y+1):
				if y % z == 0:
					list_of_divisors.append(z)
						

			for z in range(len(list_of_divisors)):
				if num % list_of_divisors[z] == 0:
					is_relatively_prime = False
					break
		if is_relatively_prime:
			num_of_relative_primes += 1
		
			
	return num_of_relative_primes


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

least_num = 0
least_psi = 0
least_ratio = 5
time.process_time()

for x in range(2,10000001):
	psi = find_psi(x)
	#if x % 100 == 0:	
	#	input("Press enter")
	
	if are_permutations(psi, x):
	#	list_of_permutations.append([psi, x])
		if x / psi < least_ratio:
			print("\nThe newest least ratio:\nThe psi is: " + str(psi) + "	The num is: " + str(x) + "	The ratio is: " + str(x/psi) + "	Time since start: " + str(time.process_time()) + "\n")
			least_ratio = x / psi
		else:
			print("The psi is: " + str(psi) + "	The num is:" + str(x) + "	The ratio is: " + str(x/psi) + " 	Time since start: " + str(time.process_time()))
			