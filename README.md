
# Nearby Test Project

An experiment to find out if its possible, using an Android app using Android
 API's, to reliably and accurately find out the distance between mobile phones in close proximity. 

The idea: if the distance is less than a specified value (ie 2m required by UK social distancing
), the app warns the phone owner to back away. Most people seem to underestimate how far 2m actually is and stand too close.
 
 The position of each android phone is determined by FusedLocationProvider running it's most
  accurate setting (FINE) and the position data is transferred between phones using the Nearby
   Connections API which under the hood makes use of Bluetooth, Bluetooth Low Energy (BLE) and
    Wifi hotspots. The Connections API was configured to run a peer2peer connection. If
     this worked then the idea would be to move to the Nearby Messages API which allows
      connections between Android and iOS devices.

# Conclusion 

The combination of fluctuations in position data greater than the range we wish to measure and
 the diffculty in maintaining connections between devices to transfer position data payloads means
  that practically, for the objective of this project, it doesnt work. The project is therefore
   abandoned.  

# Findings
  
## Location Accuracy

Expt 1: 
Using position data collected by a single phone (Samsung Galaxy A40, new in 2019) the range
 between successive location updates was calculated, ie the range between lat, long (update n
 ) and lat, long (update n-1). The time beteeen position updates was approx 5 secs. For a stationary phone, it would be
  assumed that the range between the n and n-1 locations would tend to or become zero as the
   position of the phone settled to a fixed value. 

Finding: 
Not so. The range between successive lat long returns from the FusedLocationProvider varied
 widely betwween 0m up to 7 - 8m. There was little evidence of sufficient stability in the location
  or in convergence to a fixed location value that would make the position useful in the close
   ranges required for this application. When the phones were connected this only became worse
   . Phones less than 1m away from each other produced ranges of 9m to 90m between them.

## Connection Between Phones

The connections between phones provide by the Nearby Conections API were unreliable. Connections kept dropping out and it was unclear what their status was at any time. Users have to accept the bluetooth conection and allow pairing between the devices. Even when
  this is done, paired devices would drop their connection. Getting them to reconnect was a
   bewildering process of trial and error.
   
# Kanban:

Backlog:
- [ ] add method of alerting the user to a phone close by using colour, sound and vibration
- [ ] responsive UI for landscape and portrait
- [ ] need to get the name of the user (which user adds and the app saves) or the name of the
 phone? 
- [ ] nice background graphic for the app screen
- [ ] review most accurate methods for finding the distance between devices which works on iOS
 and Android
- [ ] how best to monetise: add a scotsman voice and pay for more. Make it funny.

Done:
- [x] add send & receive methods for Nearby Connections API 
- [x] toggle button for scan on and scan off 
- [x] ensure Location is forced to use FINE rather than coarse
- [x] app icon - figures with green lightning arrow


