# Pm25TripApp
It's an app which can predict the PM2.5 and show you the PM2.5 condition for your trip in an user-friendly way APP.

## Framework


## Language used
1. client side - Java (for android studio)
2. server side - python3 (for Pm2.5 regression predict)

## What can it do
1. An user-friendly Main Menu.
2. Clear Map for the PM2.5 in color marked.
3. PM2.5 predict system.
4. Reply user as a line chart.

## How could it do
#### client side
1. Four screens for user to play.
2. Get the weather infomation from server and show by image.
3. Send the user request to server and get the line chart form server then show it.
4. Get the immediate Pm2.5 value then show on the map with different level by different level.

#### server side
1. Use the ridge regression to calculate the weight of various data.
2. Use linear regression to predict the PM2.5 value.
3. Use Google API to get Latitude ,longitude, and trip time of the user request.
4. Combine the PM2.5 value and the Google API result as a line chart.
