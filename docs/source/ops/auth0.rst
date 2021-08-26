.. Copyright (c) 2021 Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
.. SPDX-License-Identifier: Apache-2.0

Setting Up Auth0
================

In this section, we will walk through a complete setup of an entire Daml
Connect system using Auth0 as its authentication provider.

Authentication v. Authorization
-------------------------------

In a complete Daml system, the Daml components only concern themselves with
*authorization*: requests are accompanied by a (signed) token that *claims* a
number of rights (such as the right to *act as*  a given party). The Daml
system will accept these claims at face value provided that the token signature
checks out.

On the other side of the fence, the *authentication system* needs to verify a
client's identity and, based on the result, provide them with an appropriate
token. It also needs to record the mapping of client identity to Daml party (or
parties), such that the same external identity keeps mapping to the same
on-ledger party over time.

Note that we need bidirectional communication between the Daml driver and the
authentication system: the authentication system needs to contact the Daml
driver to allocate new parties when a new user logs in, and the Daml driver
needs to contact the authentication system to fetch the public key used to
verify token signatures.

In the context of this section, the authentication system is Auth0.

Prerequisites
-------------

In order to follow along this guide, you will need:

- An Auth0 tenant. See
  `Auth0 documentation<https://auth0.com/docs/get-started/create-tenants>`_ for
  how to create one if you don't have one already. You should get a free,
  dev-only one when you create an Auth0 account.
- A DNS (or IP address) that Auth0 can reach, and on which you run a JSON API
  instance. This will be used to create parties. Auth0 uses a
  `known set of IP addresses<https://auth0.com/docs/security/data-security/allowlist>`_
  that depends on the location you chose for your tenant, so if your
  application is not meant to be public you can use network rules to only let
  requests from these IPs through.
- To know the ``ledgerId`` your ledger self-identifies as. Refer to your
  specific driver's documentation for how to set the ``ledgerId`` value.
  ``ledgerId`` value with the ``--ledgerId`` parameter when starting the ledger.
- To be running SDK 1.17.0 or later. Before 1.17.0, the JSON API required an
  extra token, the setup of which is not covered here. If you are somehow
  unable to upgrade but still want to use Auth0, please contact us for
  assistance.
- An application you want to deploy on your Daml system. This is not, strictly
  speaking, required, but the whole experience is going to be a lot less
  satisfying if you don't end up with something actually running on your Daml
  Connect system. In this guide, we'll use the `create-daml-app` template,
  which as of Daml SDK 1.17.0 supports Auth0 out-of-the-box on its UI side.

Generating Party Allocation Credentials
---------------------------------------

Since Auth0 will be in charge of requesting the allocation of parties, the
first logical step is to make it generate a token that can be used to allocate
parties. This may seem recursive at first, but the token used to allocate
parties only needs to have the ``admin`` field set to ``true``; it does not
require any preexisting party and does not need any ``actAs`` or ``readAs``
privileges.

In Auth0 concepts, we first need to register
`an API <https://auth0.com/docs/get-started/set-up-apis>`_. To do so, from the
`Auth0 Dashboard <https://manage.auth0.com/>`_, open up the Applications ->
APIs page from the menu on the left and click Create API in the top right.

You can choose any name for the API; for the purposes of this document, we'll
assume this API is named ``API_NAME``. The other parameters, however, are not
free to set: the API identifier **has to be** ``https://daml.com/ledger-api``,
and the signing algorithm **has to be** RS256 (which should be selected by
default). Creating the API should automatically create a Machine-to-Machine
application "API_NAME (Test Application)", which we will be using to generate
our tokens. You can change its name to a more appropriate one; for the
remainder of this document, we will assume it is called ADMIN_TOKEN_APP.

Navigate to that application's settings page (menu on the left: Applications >
Applications page, then click on the application's name). This is where you can
rename the application and find out about its Client ID and Client Secret,
which we'll need later on.

Now that we have an API and an application, we can generate a token with the
appropriate claims. In order to do that, we need to make an Auth0 Action.

In the menu on the left, navigate to Actions > Custom, then click on Create in
the top right. You can choose an appropriate name for your action; we'll call
it ADMIN_TOKEN_ACTION. Set the Trigger field to "M2M/Client-Credentials", and
leave the version of Node tot he recommended one. (These instructions have been
tested with Node 16.)

This will open a text editor where you can add JavaScript code that will
trigger on M2M (machine to machine) connections. Replace the entire text box
content with:

.. code-block:: javascript

   exports.onExecuteCredentialsExchange = async (event, api) => {
     if (event.client.client_id === "%%ADMIN_TOKEN_ID%%") {
       api.accessToken.setCustomClaim(
         "https://daml.com/ledger-api",
         {
           "ledgerId": "%%LEDGER_ID%%",
           "participantId": null,
           "applicationId": "party-creation",
           "admin": true,
           "actAs": []
         }
       );
     }
   };

You need to replace ``%%ADMIN_TOKEN_ID%%`` with the Client ID of the
ADMIN_TOKEN_APP application, and ``%%LEDGER_ID%%`` with your actual
``ledgerId`` value. You can freely choose the ``applicationId`` value, and
should set an appropriate ``participantId`` if your Daml driver requires it.

You then need to click on Deploy in the top right to save this Action. Despite
the text on the button, this does not (yet) deploy it anywhere.

In order to actually deploy it, we need to make that Action part of a Flow. In
the menu on the left, navigate through Actions > Flows, then choose Machine to
Machine. Drag the "ADMIN_TOKEN_APP" box on the right in-between the "Start" and
"Complete" black circles in the middle. Click Apply. Now your Action is
"deployed" and, should you modify it, clicking on the Deploy button *would*
directly affect your live setup.

At this point you should be able to verify, using the curl command from the
"Quick Start" tab of the M2M application, that you get a token. You should also
be able to check that that token has the expected claims. You can do that by
piping the result of the curl command through:

.. code-block:: bash

   | jq -r '.access_token' | sed 's/.*\.\(.*\)\..*/\1/' | base64 -d

JWKS Endpoint
-------------

In order to verify the tokens it receives, the Daml driver needs to know the
public key that matches the secret key used to sign them. Daml drivers use a
standard protocol for that called JWKS; in practice, this means giving the Daml
driver an HTTP URL it can query to get the keys. In the case of Auth0, that URL
is located at ``/.well-known/jwks.json`` on the tenant.

The full address is

.. code-blocks::

   https://%%AUTH0_DOMAIN%%/.well-known/jwks.json

You can find the value for ``%%AUTH0_DOMAIN%%`` in the Domain field of the
settings page for the ADMIN_TOKEN_APP application (or any other application on
the same tenant).

Dynamic Party Allocation
------------------------

At this point, we can generate an admin token, and the Daml driver can check
its signature and thus accept it. The next step is to actually allocate
parties when people connect for the first time.

First, we need to create a new application, of type "Single Page Web
Applications". We'll be calling it LOGIN_APP. Open up the Settings tab and
scroll down to "Allowed Callback URLs". There, add your application's origin
(scheme, domain or IP, and port) to all three of Allowed Callback URLs, Allowed
Logout URLs and Allowed Web Origins. Scroll all the way down and click "Save
Changes".

Create a new Action (left menu > Actions > Custom, top-right Create button). As
usual, you can choose the name; we'll call it LOGIN_ACTION. Its type should be
"Login / Post Login".

Replace the default code with the following JavaScript:

.. code-block:: javascript

    const axios = require('axios');

    exports.onExecutePostLogin = async (event, api) => {
      if (event.client.client_id === "%%LOGIN_ID%%") {
        let party;
        if (event.user.app_metadata.party === undefined) {
          const tokenResponse = await axios.request({
            "url": "%%ADMIN_TOKEN_URL%%",
            "method": "post",
            "data": {
              "client_id": "%%ADMIN_TOKEN_ID%%",
              "client_secret": "%%ADMIN_TOKEN_SECRET%%",
              "audience": "https://daml.com/ledger-api",
              "grant_type": "client_credentials"
            },
            "headers": {
              "Content-Type": "application/json",
              "Accept": "application/json"
            }});
          const token = tokenResponse.data.access_token;
          const partyResponse = await axios.request({
            "url": "http://%%ORIGIN%%/v1/parties/allocate",
            "method": "post",
            "headers": {
              "Content-Type": "application/json",
              "Accept": "application/json",
              "Authorization": "Bearer " + token
            },
            "data": {}
          });
          party = partyResponse.data.result.identifier;
          api.user.setAppMetadata("party", party);

          // optional additional setup like creating contracts etc. here
        } else {
          party = event.user.app_metadata.party;
        }
        api.idToken.setCustomClaim("https://daml.com/ledger-api", party);
        api.accessToken.setCustomClaim(
          "https://daml.com/ledger-api",
          {
            "ledgerId": "%%LEDGER_ID%%",
            "participantId": null,
            "applicationId": "%%APP_NAME%%",
            "actAs": [party]
          });
      }
    };

where you need to replace ``%%LOGIN_ID%%`` with the Client ID of the LOGIN_APP
application; ``%%ADMIN_TOKEN_URL%%``, ``%%ADMIN_TOKEN_ID%%`` and
``%%ADMIN_TOKEN_SECRET%%`` with, respectively, the URL, ``client_id`` and
``client_secret`` values that you can find on the curl example from the Quick
Start of the ADMIN_TOKEN_APP application; ``%%ORIGIN%%`` by the domain
(or IP address) and port where Auth0 can reach your JSON API instance;
``%%LEDGER_ID%%`` by the ``ledgerId`` you're passing into your Daml driver;
``%%APP_NAME%%`` by a name of your choosing; we'll go for ``PARTY_ALLOCATION``.

Before we can click on Deploy to save (but not deploy) this snippet, we need to
do one more thing. This snippet is using a library called ``axios`` to make
HTTP calls; we need to tell Auth0 about that, so it can provision the library
at runtime.

To do that, click on the little box icon to the left of code editor, then on
the button Add Module that that revealed, and type in ``axios`` for the name
and ``0.21.1`` for the version. Then, click the Create button, and then the
Deploy button.

Now you need to go to Actions > Flows, choose the Login flow, and drag the
LOGIN_ACTION action in-between the two black circles Start and Complete.

Click Apply. You now have a working Auth0 system that automatically allocates
new parties upon first login, and remembers the mapping for future logins (that
happens by setting the party in the "app metadata", which Auth0 persists).

Token Refresh for Trigger Service
---------------------------------

If you want your users to be able to run triggers, you can run an instance of
the Trigger Service and expose it through the same HTTP URL. This means it can
use the same set of tokens, which the users will send along with their request
to start the trigger.

However, user tokens have an expiration time. In order for the trigger service
to continue running their trigger, it will need some way to refresh those
tokens.



Running Your App
----------------

For simplicity, we assume that all of the Daml components will run on a single
machine (they can find each other on ``localhost``) and that that machine has
either a public IP or a public DNS that Auth0 can reach. Furthermore, we assume
that IP/DNS is what you've configured as the callback URL above.

Finally, we assume that you can SSH into that machine and run ``daml`` and
``docker`` commands on it.

The rest of this section happens on that remote server.

First, if you don't have an app already, you can just create a new one:

.. code-block:: bash

    daml new --template=create-daml-app my-project

If you have an app already, you should be able to follow along. However, if
your app was based on the ``create-daml-app`` template using a Daml SDK version
prior to 1.17.0, you may need to adapt your ``ui/src/config.ts`` and
``ui/src/components/LoginScreen.tsx`` files. See `this commit <>`_ for
guidance.

Next, we need to start the Daml driver. For this example we'll use the sandbox,
but with ``--implicit-party-allocation false`` it should behave like a
production ledger (minus persistence).

.. code-blocks:: bash

    cd my-project
    daml build
    daml codegen js .daml/dist/my-project-0.1.0.dar -o ui/daml.js
    daml sandbox --ledgerid %%LEDGER_ID%% \
                 --auth-jwt-rs256-jwks https://%%AUTH0_DOMAIN%%/.well-known/jwks.json \
                 --implicit-party-allocation false \
                 .daml/dist/my-project-0.1.0.dar

As before, you need to replace ``%%LEDGER_ID%%`` with a value of your choosing
(the same one you used when configuring Auth0), and ``%%AUTH0_DOMAIN%%`` with
your Auth0 domain, which you can find as the Domain field at the top of the
Settings tab for any app in the tenant.

Next, you need to start a JSON API instance.

.. code-block:: bash

    cd my-project
    daml json-api --ledger-port 6865 \
                  --ledger-host localhost \
                  --http-port 4000

If you are using a Daml SDK version prior to 1.17.0, you'll need to find a way
to supply the JSON API with a valid, refreshing token file. We recommend
upgrading to 1.17.0 or later.

Next, let's build our frontend code:

.. code-block:: bash

    cd my-project/ui
    npm install
    REACT_APP_AUTH=auth0 \
    REACT_APP_AUTH0_DOMAIN=%%AUTH0_DOMAIN%% \
    REACT_APP_AUTH0_CLIENT_ID=%%LOGIN_ID%% \
    npm run-script build

As before, ``%%AUTH0_DOMAIN%%`` and ``%%LOGIN_ID%%`` need to be replaced.

Now, we need to expose the JSON API and our static files. We'll use ``docker``
for that, but you can use any HTTP server you (and your security team) are
comfortable with, as long as it can serve static files and proxy some paths.

First, create a file ``nginx/nginx.conf.sh`` with the following content next to
your app folder (``my-project`` in this example):

.. code-block::

    #!/usr/bin/env bash

    set -euo pipefail
    openssl req -x509 \
                -newkey rsa:4096 \
                -keyout /etc/ssl/private/nginx-selfsigned.key \
                -out /etc/ssl/certs/nginx-selfsigned.crt \
                -days 365 \
                -nodes \
                -subj "/C=US/ST=Oregon/L=Portland/O=Company Name/OU=Org/CN=${FRONTEND_IP}"
    openssl dhparam -out /etc/ssl/certs/dhparam.pem 2048
    cat <<NGINX_CONFIG > /etc/nginx/nginx.conf
    worker_processes auto;
    pid /run/nginx.pid;
    events {
      worker_connections 768;
    }
    http {
      sendfile on;
      tcp_nopush on;
      tcp_nodelay on;
      keepalive_timeout 65;
      types_hash_max_size 2048;
      include /etc/nginx/mime.types;
      default_type application/octet-stream;
      access_log /var/log/nginx/access.log;
      error_log /var/log/nginx/error.log;
      gzip on;

      ssl_certificate /etc/ssl/certs/nginx-selfsigned.crt;
      ssl_certificate_key /etc/ssl/private/nginx-selfsigned.key;
      ssl_protocols TLSv1 TLSv1.1 TLSv1.2;
      ssl_prefer_server_ciphers on;
      ssl_ciphers "EECDH+AESGCM:EDH+AESGCM:AES256+EECDH:AES256+EDH";
      ssl_ecdh_curve secp384r1;
      ssl_session_cache shared:SSL:10m;
      ssl_session_tickets off;
      ssl_stapling on;
      ssl_stapling_verify on;
      resolver 8.8.8.8 8.8.4.4 valid=300s;
      resolver_timeout 5s;
      add_header X-Frame-Options DENY;
      add_header X-Content-Type-Options nosniff;

      ssl_dhparam /etc/ssl/certs/dhparam.pem;

      server {
        listen 80;
        return 302 https://${FRONTEND_IP}\$request_uri;
      }

      server {
        listen 443 ssl http2;
        location /v1/stream {
          proxy_pass http://${JSON_IP};
          proxy_http_version 1.1;
          proxy_set_header Upgrade \$http_upgrade;
          proxy_set_header Connection "Upgrade";
          proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        }
        location /v1 {
          proxy_pass http://${JSON_IP};
          proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        }
        root /app/ui;
        index index.html;
        location / {
          try_files \$uri \$uri/ =404;
        }
      }
    }
    NGINX_CONFIG

Next, create a file ``nginx/Dockerfile`` with this content:

.. code-block::

    FROM nginx:1.21.0

    COPY build /app/ui
    COPY nginx.conf.sh /app/nginx.conf.sh
    RUN chmod +x /app/nginx.conf.sh
    CMD /app/nginx.conf.sh && exec nginx -g 'daemon off;'

Finally, we can build and run the Docker container with the following, starting
in the folder that contains both ``nginx`` and ``my-project``:

.. code-block:: bash

    cp -r my-project/ui/build nginx/build
    cd nginx
    docker build -t frontend .
    docker run -e JSON_IP=localhost:4000 -e FRONTEND_IP=%%ORIGIN%% --network=host frontend


