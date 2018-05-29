CheckIn
=======

Shows Scan card or type in name 
Has autocomplete for member list for manual checking.

shows selected member for checkin. 

Shows  checked in members for today

when you start to checking, wether by card or name, it checks if you are already checked in for today, so you don't pay double
admin members will have the option to check out another member and return balance

maybe the member lookup should be on home, then start checking the found member

text: Scan your smartcard or start typing your name here to find your member acount

new member: 2 ways: scan a card and select a member, or scan a card and enter add new member

attach card to member. must create member first, then scan card

card is scanned, but no member found: dialog new card found, pick a member to assign the card to

after card is scanned , show if user is already checked in.
if not show payment options for check in

next to selected member to check in have a cancel field to clear selected member



If you have a smartcard, hold it to the side

No member yet? Create an account

Become a member dialog

left side:Select member to check in


	search by name autocomplete

	Add new member	button to trigger dialog

	on bottom Member list not checked in scroll view



right side current member payments to comlete checkin

	if selected 
		Current user / welcome <NAME>
		fee is $5 today, and your balance is $-5
		Select amount: 5 10 20


	on bottom Member list checked in today scroll view


add member button pops up dialog and loads current member into member checkin view
card scan loads found member into member checkin view
if not found, dialog pops up and asks which member the card should be assigned to. will overwrite current card, old car will be moved to unused cards stack.



Member Document
==================
balance
20
(number)
email
"opendroid@gmail.com"
firstName
"Daniel"
isActive
true
isMailingSubscriber
true
lastName
"Knapp"
smartcardId
"C1C53D16"

card Scan:
if id not found, ask if you want to add a member and pass the id as bundle to the activity. which one? main? or member and then pass the member back to checkin?


auth: kein login, wegen nutzung offline.
stattdessen master passwort hinterlegen und in einem dialog abfragen bei akttionen wie loeschen etc.

++++++++++++++++++
NEXT STEPS
++++++++++++++++++
onclick listener to member list
test layout for playin today list in right half of screen
	better: just signal if the player is playing today in main member list
	or: GridLayoutManager for recyclerview, and order by lastCheckIn, show icon if its today. participants not checkin in often move down the list

build form for check in


check in per dialog, money per radio button. check if we can have a selection AND a checkbox in the same dialog
playing today per green checkmark in table. make a headline

onselectionchange listener und die fragen nach change ein/ausblenden

icon in checkin dialog aendern, title aendern
Welcome Daniel Knapp!
The fee for playing today is $5. Your current balance is $0

pass member object to checkIn dialog as parcelable bundle

remove side layout, make gridlayout for member data, hook up smart card
add checks for dialogs so you have to fill in fields . set OK to disabled

fix bug on orientation change dialog pop ups again.

dedicated payment table so you can do accounting

name in checkin dialog bigger

admin logins, if auth then special functions
make member records editable for public
if auth then decide when card is scanned: assign card to member or check in
or can you scan a card with a special intent to assign it.

OK play this week instead of today

no auth when offline, use smartcard as auth. if card member is admin, show bottomdialofgsheet to select what to do
view and edit member list
show transactions and total balance
enter transaction with description, plus and minus so admin can put in money



