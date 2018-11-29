# SOFE4790DSProject

Main Project in JeroProject

Run in Eclipse

Required Jars: commons-codec-1.11.jar and jeromq.jar (found outside of JeroProject folder)

Run two windows of PubsubUI for the Reg Pub Sub methods or the New Pub Sub methods

NOTES:

Do not mix Reg and New methods as they do not work with each other

Pub methods need to be run before Sub methods

For New Pub and Sub when running two windows for each, you must alternate between the publisher and subscriber execute buttons. This is not a problem in the main tests of pub and sub because they use while loops. To have a properly working JFrame UI, methods cannot have while loops that continuously run since the window won't be able to update, thus freezing the application but running in the background. 

When running in eclipse, you may need to clean the project for the jars to be seen. 

When running testMainPub, use pub.publishEncrypted("test","test") for new method and pub.publish("test","test") for old method

When running testMainSub, use sub.getSubscriptionEncrypted("test") for new method and sub.getSubscription("test") for old method
