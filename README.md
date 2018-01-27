# PGPAuth
[![GitHub version](https://badge.fury.io/gh/PGPAuth%2FPGPAuth_Android.svg)](http://badge.fury.io/gh/PGPAuth%2FPGPAuth_Android) 

## What is it?
PGPAuth is an App to send PGP signed requests to servers.

Currently the actions to send are hardcoded to "open" and "close", but this is planned to be configurable on the server. If you want this feature, please comment in #3.

## Use cases

* digital key for doors (i.e. for small organizations or hacker spaces)
* beer taps (configure one server per tap and have IoT beer taps)
  * people actually did that

## How to use it?

The smartphones having this app installed need direct access to the server in question. Communication is done over HTTP(S). 

Requests are POSTed to the server. For details, please look at the reference implementation at https://github.com/PGPAuth/PGPAuth_CGI.


## How can I help?
* use it and provide feedback
* help with translation: https://www.transifex.com/projects/p/pgpauth/
* work on reported issues
* write docs
* create implementations for other systems
