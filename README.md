SmartComiX
===========

Code written in 2015 as a commission for an Italian publishing house (Shockdom s.r.l.).
It's a store for comic books tailored for phone screens. Each story available in two formats: only images (SmartComiX) and images + audio (SmartSoniX).
Runs on Android minSdk >= 15 (targetSdk 23).

The app is available on Google Play: https://goo.gl/NwgBTH

Installation/Setup
------------------

Run the project from AndroidStudio or from command line using Gradle.
However this code won't compile without filling the `*.properties` files omitted from the codebase for obvious reasons. The requested files are:

```
./app/signing.properties
./app/server.properties
```

If you want to provide your version of the files (this means providing also your version of the server, whose code is not public yet) you can create those from the corresponding `*.dummy` files, which contains the template for the information you need to provide.

Dependencies
------------

I'm using [Android Studio](http://tools.android.com/download/studio/builds/2-3-0) and [Gradle](https://docs.gradle.org/3.4.1/release-notes.html) to build.
All the code was written in Java.

Other dependencies:

* Android Support Library v7 (AppCompat, RecyclerView, CardView, Design for Snackbars) v23.0.1
* Square's Retrofit v1.9.0
* Square's OkHttp v2.2.0
* Facebook's Fresco v0.7.0
* Square's Retrofit v1.9.0
* GreenRobot Event Bus v2.4.0
* Anjlab's In-App Billing v3 Library v1.0.24

Known Issues
------------

Anjlab's In-App Billing v3 Library has some incompatibility with Nougat and its multi-account feature, so the purchase may not work because of this reason, see [Reported Issue](https://github.com/anjlab/android-inapp-billing-v3/issues/202).