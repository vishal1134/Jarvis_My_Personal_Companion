# Recognized Words

Jarvis currently uses a rule-based parser. These are the main words and phrase
patterns it recognizes today.

## Wake Names

Default:

```text
jarvis
```

Wake names are dynamic. Jarvis can store up to 3 active names in memory.
The wake name can be spoken at the start or end:

```text
Jarvis time enna
time ena Jarvis
torch off pannu Jarvis
```

## Language Hints

These words help Jarvis detect Tamil-English style commands:

```text
pannu
pannunga
ku
la
pesu
koopidu
thirakku
thira
sollu
sollunga
enna
ena
neram
thethi
indru
udhavi
niruthu
podu
venum
mudi
mudiyum
```

Tamil script characters are also detected.

## Contact Calling

Recognized words:

```text
call
call pannu
koopidu
ku call pannu
```

Examples:

```text
Jarvis call appa
appa ku call pannu
appa koopidu
appa ku phone pannu
appa phone podu
```

## Opening Apps

Recognized words:

```text
open
open pannu
thirakku
```

Examples:

```text
Jarvis open whatsapp
whatsapp open pannu
calculator thirakku
settings thira
whatsapp thira
```

## YouTube Search

Recognized words:

```text
youtube search
search in youtube
in youtube
youtube la
search pannu
```

Examples:

```text
Jarvis search vijay songs in youtube
Jarvis youtube search tamil songs
Jarvis youtube la tamil songs search pannu
Jarvis youtube la tamil songs thedu
Jarvis youtube la munbe vaa search pannu
Jarvis youtube la cooking video search
```

## Notifications

Recognized words:

```text
read notifications
notification read
notifications read
notification padi
notifications padi
```

Examples:

```text
Jarvis read notifications
notifications read pannu
notifications padi
notifications sollu
notification enna
whatsapp la yaaru enaku message pannirukaa Jarvis
Jarvis whom have messaged me in whatsapp
```

## Wake And Status

Recognized words:

```text
wake up
are you there
hello
hi
online
status
listen
start listening
irukiya
irukingala
kelu
ready ah
```

Examples:

```text
Jarvis
Jarvis are you there
Jarvis status
Jarvis irukiya
```

## Help And Control

Recognized words:

```text
help
what can you do
list commands
show commands
repeat
say that again
stop
cancel
be quiet
udhavi
enna panna mudiyum
commands sollu
marubadi sollu
thirumba sollu
niruthu
pesatha
```

Examples:

```text
Jarvis what can you do
Jarvis enna panna mudiyum
Jarvis repeat
Jarvis marubadi sollu
Jarvis stop
Jarvis niruthu
```

## Time And Date

Recognized words:

```text
time
what time is it
current time
date
today date
what is the date
neram enna
time sollu
mani enna
date sollu
thethi enna
innaiku date enna
```

Examples:

```text
Jarvis what time is it
Jarvis today date
Jarvis neram enna
time ena Jarvis
Jarvis thethi enna
```

## Phone Settings

Recognized words:

```text
settings
wifi settings
connect to wifi
bluetooth settings
notification settings
accessibility settings
settings thira
wifi thira
wifi open pannu
bluetooth thira
notification settings thira
```

Examples:

```text
Jarvis open settings
Jarvis wifi settings
Jarvis connect to wifi
Jarvis bluetooth settings
Jarvis wifi thira
```

## Bluetooth Control

Recognized words:

```text
bluetooth on
bluetooth on pannu
turn on bluetooth
bluetooth off
bluetooth off pannu
connect bluetooth
bluetooth connect pannu
connect to ... bluetooth
```

Examples:

```text
Jarvis bluetooth on pannu
Jarvis bluetooth off pannu
Jarvis sony headphones bluetooth connect pannu
Jarvis connect to car bluetooth
```

Android note:

```text
Jarvis opens Bluetooth settings. Android may require you to confirm connection manually.
```

## Wi-Fi Control

Recognized words:

```text
wifi on
wifi on pannu
turn on wifi
wifi off
wifi off pannu
connect to
connect pannu
wifi password vandhu
password is
```

Examples:

```text
Jarvis wifi on pannu
turn on wifi Jarvis
Jarvis wifi off pannu
Jarvis connect to homewifi
Jarvis homewifi connect pannu wifi password vandhu kowsalya at 05 k capital letter
```

Password spoken symbols:

```text
at -> @
hash -> #
dollar -> $
dot -> .
underscore -> _
dash -> -
star -> *
```

Capital letters:

```text
kowsalya at 05 k capital letter -> Kowsalya@05
```

## Flashlight

Recognized words:

```text
turn on flashlight
flashlight on
torch on
turn on torch
turn off flashlight
flashlight off
torch off
turn off torch
torch on pannu
torch off pannu
torch podu
torch niruthu
```

Examples:

```text
Jarvis turn on flashlight
Jarvis torch off
Jarvis torch podu
Jarvis torch on pannu
torch off pannu Jarvis
Jarvis torch niruthu
```

## Language Switching

Recognized words:

```text
reply in tamil
tamil la reply
reply in english
english la reply
```

Examples:

```text
reply in tamil
english la reply pannu
```

## Assistant Name Management

Recognized words:

```text
change your name to
rename yourself to
your name is now
add ... as your name
remove
```

Examples:

```text
Jarvis change your name to Friday
Jarvis add Friday as your name
Jarvis remove Friday
```

## Sleep Mode

Recognized words:

```text
turn off
go offline
sleep
thoongu
```

Examples:

```text
Jarvis turn off
Jarvis sleep
```

After this, Jarvis ignores commands without the wake name. It responds again
when you say:

```text
Jarvis
Jarvis wake up
Jarvis irukiya
```

## Multiple Commands

Recognized separators:

```text
and then
then
and
appuram
apram
athukapram
```

Examples:

```text
Jarvis time enna and torch on pannu
Jarvis open whatsapp appuram notification ena
```
