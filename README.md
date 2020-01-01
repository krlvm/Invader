# Invader
An effective MITM utility and script injector

Invader is built around PowerTunnel v1.7.1 and provides ability to inject a custom JavaScript to any website using your own self-signed MITM Root CA and block any URL you want.

## Getting started
### Download Invader
Download a binary from the `Releases` tab or build it yourself.
You need Java 7+.

### Configure Invader
Launch Invader and try to start the proxy server. There will be created some configuration files. Open `cert-password.txt` and write there invented by you strong certificate password. Certificate will be generated automatically.

Setup your OS/browser proxy server with data from the Invader frame. Then you need to add your self-signed certificate to your OS keychain.

## Injecting scripts
In `scripts` folder you can find `main.js`. It is a script, that will be injected to all webpages. If you need to inject a some script to a specific webpage you can create there a `.js` file and write it to the `script-map.txt`. Example: `github.com:github-script`, i.e. you have to create `github-script.js` in `scripts` folder. You have to write only the script name.

If you need no global script, just left `main.js` empty.

## Traffic sniffer
Since v1.3 Invader is shipping with a built-in sniffer module. Use argument `-with-sniffer` to activate it. You're can view sniffed traffic at Invader Monitor - http://invadermitmmonitor.info/sniffer. This is a fake address available only when you are connected to proxy.

If you want to render sniffed HTML content add an argument -render-sniffed-content.

## Dependencies
This project is made possible by [PowerTunnel](https://github.com/krlvm/PowerTunnel), modified [LittleProxy-MITM](https://github.com/ganskef/LittleProxy-mitm) and [SwingDPI](https://github.com/krlvm/SwingDPI).