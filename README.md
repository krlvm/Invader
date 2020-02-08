<div align="center">
<img src="https://raw.githubusercontent.com/krlvm/Invader/master/images/logo.png" height="192px" width="192px" />
<br><h1>Invader</h1><br>
An effective MITM utility and script injector
<br><br>
<a href="https://github.com/krlvm/Invader/blob/master/LICENSE"><img src="https://img.shields.io/github/license/krlvm/Invader?style=flat-square" alt="License"/></a>
<a href="https://github.com/krlvm/Invader/releases"><img src="https://img.shields.io/github/v/release/krlvm/Invader?style=flat-square" alt="Latest release"/></a>
<a href="https://github.com/krlvm/Invader/releases"><img src="https://img.shields.io/github/downloads/krlvm/Invader/total?style=flat-square" alt="Downloads"/></a>
<br>
<img src="https://raw.githubusercontent.com/krlvm/Invader/master/images/ui.png" alt="Invader User Interface" />
</div>

## Getting started
Invader is built around PowerTunnel v1.8.4 and provides ability to inject a custom JavaScript to any website using your own self-signed MITM Root CA and block any URL you want.

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