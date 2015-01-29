# PGPAuth

## What is it?
It is an App to send a PGP-signed request to a server to open or close $things.
A request consists of an action (for example "open" or "close") and a timestamp, so you can configure how long a request is valid.
The whole request is then signed with PGP (currently using the APG App on Android) and sent to the specified Server via HTTP-POST.

## How to use it?
You'll need to have a web-accessible HTTP-Server with support for server-side programs (CGI or alike [PHP, ASP, ...]).
You will get the signed message via the "data"-POST-parameter.
Then you can check if the signature is valid, the signature key is authorised and the timestamp is not that long ago it's already invalid.
When everything is checked, you will have to extract the request from the message and do something with it.

You can use PGPAuth_CGI to handle requests:
https://github.com/PGPAuth/PGPAuth_CGI

## How can I help?
You can use it and provide feedback if you wish a feature or catched a bug.
You can also help with translation, just go to https://www.transifex.com/projects/p/pgpauth/ 
