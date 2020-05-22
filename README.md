
# Nearby Test Project

A first draft of an android app which uses its location and the location received from a nearby
 phone to calculate the distance between them. Uses FINE location data.
 
If the distance is less than a specified value (ie 2m required by UK social distancing), the app
 warns the phone owner to back away. Most people seem to underestimate how far 2m actually is and stand too close.

Using Nearby Connections API using peer2peer strategy (will communicate only between android)


To Do:

- [ ] add send & receive methods for Nearby Connections API 
- [ ] add method of alerting the user to a phone close by using colour, sound and vibration
- [ ] responsive UI for landscape and portrait
- [ ] need to get the name of the user (which user adds and the app saves) or the name of the
 phone? 
- [ ] nice background graphic for the app screen
- [ ] review most accurate methods for finding the distance between devices which works on iOS
 and Android
- [ ] how best to monetise: add a scotsman voice and pay for more. Make it funny.

- [x] toggle button for scan on and scan off 
- [x] ensure Location is forced to use FINE rather than coarse
- [x] app icon - figures with green lightning arrow


