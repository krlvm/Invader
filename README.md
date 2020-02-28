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
Invader is based on PowerTunnel v1.8.4 and provides abilities related to the MITM attack:
* decrypt HTTPS traffic using your own self-signed MITM Root CA
* inject a custom JavaScript to any website
* Invader provides a JavaScript Hook API that allows you to write code for request/response filtering (more about that below)
* sniff HTTP/HTTPS traffic and display it on a local website

PowerTunnel v1.9.2 codebase update is planned for April 2020.

### Download Invader
Download a binary from the `Releases` tab or build it yourself.
You need Java 7+.

### Setup
The installation process, mostly, is identical to the PowerTunnel installation process and described in detail [on the Wiki](https://github.com/krlvm/PowerTunnel/wiki/Installation).

### Configure Invader
Launch Invader and try to start the proxy server. There will be created some configuration files. Open `cert-password.txt` and write there invented by you strong certificate password. Certificate will be generated automatically.

Setup your OS/browser proxy server with data from the Invader (IP and Port). Then you need to add your self-signed certificate to your OS keychain.

## Injecting scripts
In `scripts` folder you can find `main.js`. It is a script, that will be injected to all websites. If you need to inject some script only to a specific website you can create there a `.js` file and write it to the `script-map.txt`. Example: `github.com:github-script`, i.e. you have to create `github-script.js` in `scripts` folder. You have to write only the script name.

If you need no global script, just left `main.js` empty.

## JavaScript Hooks
JavaScript Hooks was added in version 1.5 - this is something like plugins.

Hooks is disabled by default. Create a file `hook.js` in the Invader folder to enable this feature.

Hooks are executing in the *Nashorn* - the Java JS Virtual Machine. It does not support ECMAScript 6 and demand strict JavaScript syntax - it will throw an error if you forgot to add semicolon at the line end.

After the file is created, write there these code:
```js
function onRequest(headers) {
    return [ headers ];
}

function onResponse(headers, host, data, status) {
    return [ headers, data ];
}
```
This code, as you see, do nothing with the packets.

Variables:
* `headers` - object with properties, i.e. `headers['Host']` will return `google.com` in `onRequest`, for example.
* `host` - host, note that host doesn't contains in the response headers
* `data` - content of the packet
* `status` - HTTP code, e.g. 200

If you do not need one of these functions, just remove it.

Some examples:
#### Simple HTTP DPI circumvention
```js
function onRequest(headers) {
    headers['hOsT'] = headers['Host'] + '.';
    headers.remove('Host');
    return [ headers ];
}
```

#### Bingroll from Google:
##### Variant 1 (using onRequest)
```js
function onRequest(headers) {
    if(headers['Host'].contains('google.com') {
        headers['Location'] = 'https://bing.com';
        return [ headers, 'You've been bingrolled', 301 ];
    }
    return [ headers ];
}
```
##### Variant 2 (using onResponse)
```js
function onResponse(headers, host, data, status) {
    if(host.contains('google.com')) {
        headers['Location'] = 'https://bing.com';
        return [ headers, 'You've been bingrolled', 301 ];
    }
    return [ headers, data ];
}
```

## Traffic sniffer
Invader is shipping with a built-in sniffer module. Use argument `-with-sniffer [appendix]` to activate it, where appendix - any word. You're can view sniffed traffic at Invader Monitor - `http://invadermitmmonitor[appendix].info/sniffer`. This is a fake address available only when you are connected to proxy.

If you want to render sniffed HTML content add an argument -render-sniffed-content.

## Dependencies
* [PowerTunnel](https://github.com/krlvm/PowerTunnel) - codebase (v1.8.4)
* [LittleProxy-MITM](https://github.com/ganskef/LittleProxy-mitm) with some [modifications](https://github.com/krlvm/Invader/tree/master/src/org/littleshoot/proxy) - proxy server
* [SwingDPI](https://github.com/krlvm/SwingDPI) - HiDPI scaling
