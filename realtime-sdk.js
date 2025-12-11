!function(t, e) {
    "object" == typeof exports && "object" == typeof module ? module.exports = e() : "function" == typeof define && define.amd ? define([], e) : "object" == typeof exports ? exports.realtime = e() : t.realtime = e()
}(this, function() {
    return function(t) {
        function e(r) {
            if (n[r])
                return n[r].exports;
            var o = n[r] = {
                i: r,
                l: !1,
                exports: {}
            };
            return t[r].call(o.exports, o, o.exports, e),
            o.l = !0,
            o.exports
        }
        var n = {};
        return e.m = t,
        e.c = n,
        e.i = function(t) {
            return t
        }
        ,
        e.d = function(t, n, r) {
            e.o(t, n) || Object.defineProperty(t, n, {
                configurable: !1,
                enumerable: !0,
                get: r
            })
        }
        ,
        e.n = function(t) {
            var n = t && t.__esModule ? function() {
                return t.default
            }
            : function() {
                return t
            }
            ;
            return e.d(n, "a", n),
            n
        }
        ,
        e.o = function(t, e) {
            return Object.prototype.hasOwnProperty.call(t, e)
        }
        ,
        e.p = "",
        e(e.s = 54)
    }([function(t, e, n) {
        function r(t) {
            if (t)
                return o(t)
        }
        function o(t) {
            for (var e in r.prototype)
                t[e] = r.prototype[e];
            return t
        }
        t.exports = r,
        r.prototype.on = r.prototype.addEventListener = function(t, e) {
            return this._callbacks = this._callbacks || {},
            (this._callbacks["$" + t] = this._callbacks["$" + t] || []).push(e),
            this
        }
        ,
        r.prototype.once = function(t, e) {
            function n() {
                this.off(t, n),
                e.apply(this, arguments)
            }
            return n.fn = e,
            this.on(t, n),
            this
        }
        ,
        r.prototype.off = r.prototype.removeListener = r.prototype.removeAllListeners = r.prototype.removeEventListener = function(t, e) {
            if (this._callbacks = this._callbacks || {},
            0 == arguments.length)
                return this._callbacks = {},
                this;
            var n = this._callbacks["$" + t];
            if (!n)
                return this;
            if (1 == arguments.length)
                return delete this._callbacks["$" + t],
                this;
            for (var r, o = 0; o < n.length; o++)
                if ((r = n[o]) === e || r.fn === e) {
                    n.splice(o, 1);
                    break
                }
            return 0 === n.length && delete this._callbacks["$" + t],
            this
        }
        ,
        r.prototype.emit = function(t) {
            this._callbacks = this._callbacks || {};
            for (var e = new Array(arguments.length - 1), n = this._callbacks["$" + t], r = 1; r < arguments.length; r++)
                e[r - 1] = arguments[r];
            if (n) {
                n = n.slice(0);
                for (var r = 0, o = n.length; r < o; ++r)
                    n[r].apply(this, e)
            }
            return this
        }
        ,
        r.prototype.listeners = function(t) {
            return this._callbacks = this._callbacks || {},
            this._callbacks["$" + t] || []
        }
        ,
        r.prototype.hasListeners = function(t) {
            return !!this.listeners(t).length
        }
    }
    , function(t, e, n) {
        function r(t, n) {
            return n("b" + e.packets[t.type] + t.data.data)
        }
        function o(t, n, r) {
            if (!n)
                return e.encodeBase64Packet(t, r);
            var o = t.data
              , i = new Uint8Array(o)
              , s = new Uint8Array(1 + o.byteLength);
            s[0] = v[t.type];
            for (var a = 0; a < i.length; a++)
                s[a + 1] = i[a];
            return r(s.buffer)
        }
        function i(t, n, r) {
            if (!n)
                return e.encodeBase64Packet(t, r);
            var o = new FileReader;
            return o.onload = function() {
                e.encodePacket({
                    type: t.type,
                    data: o.result
                }, n, !0, r)
            }
            ,
            o.readAsArrayBuffer(t.data)
        }
        function s(t, n, r) {
            if (!n)
                return e.encodeBase64Packet(t, r);
            if (m)
                return i(t, n, r);
            var o = new Uint8Array(1);
            return o[0] = v[t.type],
            r(new C([o.buffer, t.data]))
        }
        function a(t) {
            try {
                t = d.decode(t, {
                    strict: !1
                })
            } catch (t) {
                return !1
            }
            return t
        }
        function c(t, e, n) {
            for (var r = new Array(t.length), o = l(t.length, n), i = function(t, n, o) {
                e(n, function(e, n) {
                    r[t] = n,
                    o(e, r)
                })
            }, s = 0; s < t.length; s++)
                i(s, t[s], o)
        }
        var u, f = n(41), h = n(15), p = n(29), l = n(28), d = n(42);
        "undefined" != typeof ArrayBuffer && (u = n(31));
        var y = "undefined" != typeof navigator && /Android/i.test(navigator.userAgent)
          , g = "undefined" != typeof navigator && /PhantomJS/i.test(navigator.userAgent)
          , m = y || g;
        e.protocol = 3;
        var v = e.packets = {
            open: 0,
            close: 1,
            ping: 2,
            pong: 3,
            message: 4,
            upgrade: 5,
            noop: 6
        }
          , b = f(v)
          , w = {
            type: "error",
            data: "parser error"
        }
          , C = n(33);
        e.encodePacket = function(t, e, n, i) {
            "function" == typeof e && (i = e,
            e = !1),
            "function" == typeof n && (i = n,
            n = null);
            var a = void 0 === t.data ? void 0 : t.data.buffer || t.data;
            if ("undefined" != typeof ArrayBuffer && a instanceof ArrayBuffer)
                return o(t, e, i);
            if (void 0 !== C && a instanceof C)
                return s(t, e, i);
            if (a && a.base64)
                return r(t, i);
            var c = v[t.type];
            return void 0 !== t.data && (c += n ? d.encode(String(t.data), {
                strict: !1
            }) : String(t.data)),
            i("" + c)
        }
        ,
        e.encodeBase64Packet = function(t, n) {
            var r = "b" + e.packets[t.type];
            if (void 0 !== C && t.data instanceof C) {
                var o = new FileReader;
                return o.onload = function() {
                    var t = o.result.split(",")[1];
                    n(r + t)
                }
                ,
                o.readAsDataURL(t.data)
            }
            var i;
            try {
                i = String.fromCharCode.apply(null, new Uint8Array(t.data))
            } catch (e) {
                for (var s = new Uint8Array(t.data), a = new Array(s.length), c = 0; c < s.length; c++)
                    a[c] = s[c];
                i = String.fromCharCode.apply(null, a)
            }
            return r += btoa(i),
            n(r)
        }
        ,
        e.decodePacket = function(t, n, r) {
            if (void 0 === t)
                return w;
            if ("string" == typeof t) {
                if ("b" === t.charAt(0))
                    return e.decodeBase64Packet(t.substr(1), n);
                if (r && !1 === (t = a(t)))
                    return w;
                var o = t.charAt(0);
                return Number(o) == o && b[o] ? t.length > 1 ? {
                    type: b[o],
                    data: t.substring(1)
                } : {
                    type: b[o]
                } : w
            }
            var i = new Uint8Array(t)
              , o = i[0]
              , s = p(t, 1);
            return C && "blob" === n && (s = new C([s])),
            {
                type: b[o],
                data: s
            }
        }
        ,
        e.decodeBase64Packet = function(t, e) {
            var n = b[t.charAt(0)];
            if (!u)
                return {
                    type: n,
                    data: {
                        base64: !0,
                        data: t.substr(1)
                    }
                };
            var r = u.decode(t.substr(1));
            return "blob" === e && C && (r = new C([r])),
            {
                type: n,
                data: r
            }
        }
        ,
        e.encodePayload = function(t, n, r) {
            function o(t) {
                return t.length + ":" + t
            }
            function i(t, r) {
                e.encodePacket(t, !!s && n, !1, function(t) {
                    r(null, o(t))
                })
            }
            "function" == typeof n && (r = n,
            n = null);
            var s = h(t);
            return n && s ? C && !m ? e.encodePayloadAsBlob(t, r) : e.encodePayloadAsArrayBuffer(t, r) : t.length ? void c(t, i, function(t, e) {
                return r(e.join(""))
            }) : r("0:")
        }
        ,
        e.decodePayload = function(t, n, r) {
            if ("string" != typeof t)
                return e.decodePayloadAsBinary(t, n, r);
            "function" == typeof n && (r = n,
            n = null);
            var o;
            if ("" === t)
                return r(w, 0, 1);
            for (var i, s, a = "", c = 0, u = t.length; c < u; c++) {
                var f = t.charAt(c);
                if (":" === f) {
                    if ("" === a || a != (i = Number(a)))
                        return r(w, 0, 1);
                    if (s = t.substr(c + 1, i),
                    a != s.length)
                        return r(w, 0, 1);
                    if (s.length) {
                        if (o = e.decodePacket(s, n, !1),
                        w.type === o.type && w.data === o.data)
                            return r(w, 0, 1);
                        if (!1 === r(o, c + i, u))
                            return
                    }
                    c += i,
                    a = ""
                } else
                    a += f
            }
            return "" !== a ? r(w, 0, 1) : void 0
        }
        ,
        e.encodePayloadAsArrayBuffer = function(t, n) {
            function r(t, n) {
                e.encodePacket(t, !0, !0, function(t) {
                    return n(null, t)
                })
            }
            if (!t.length)
                return n(new ArrayBuffer(0));
            c(t, r, function(t, e) {
                var r = e.reduce(function(t, e) {
                    var n;
                    return n = "string" == typeof e ? e.length : e.byteLength,
                    t + n.toString().length + n + 2
                }, 0)
                  , o = new Uint8Array(r)
                  , i = 0;
                return e.forEach(function(t) {
                    var e = "string" == typeof t
                      , n = t;
                    if (e) {
                        for (var r = new Uint8Array(t.length), s = 0; s < t.length; s++)
                            r[s] = t.charCodeAt(s);
                        n = r.buffer
                    }
                    o[i++] = e ? 0 : 1;
                    for (var a = n.byteLength.toString(), s = 0; s < a.length; s++)
                        o[i++] = parseInt(a[s]);
                    o[i++] = 255;
                    for (var r = new Uint8Array(n), s = 0; s < r.length; s++)
                        o[i++] = r[s]
                }),
                n(o.buffer)
            })
        }
        ,
        e.encodePayloadAsBlob = function(t, n) {
            function r(t, n) {
                e.encodePacket(t, !0, !0, function(t) {
                    var e = new Uint8Array(1);
                    if (e[0] = 1,
                    "string" == typeof t) {
                        for (var r = new Uint8Array(t.length), o = 0; o < t.length; o++)
                            r[o] = t.charCodeAt(o);
                        t = r.buffer,
                        e[0] = 0
                    }
                    for (var i = t instanceof ArrayBuffer ? t.byteLength : t.size, s = i.toString(), a = new Uint8Array(s.length + 1), o = 0; o < s.length; o++)
                        a[o] = parseInt(s[o]);
                    if (a[s.length] = 255,
                    C) {
                        var c = new C([e.buffer, a.buffer, t]);
                        n(null, c)
                    }
                })
            }
            c(t, r, function(t, e) {
                return n(new C(e))
            })
        }
        ,
        e.decodePayloadAsBinary = function(t, n, r) {
            "function" == typeof n && (r = n,
            n = null);
            for (var o = t, i = []; o.byteLength > 0; ) {
                for (var s = new Uint8Array(o), a = 0 === s[0], c = "", u = 1; 255 !== s[u]; u++) {
                    if (c.length > 310)
                        return r(w, 0, 1);
                    c += s[u]
                }
                o = p(o, 2 + c.length),
                c = parseInt(c);
                var f = p(o, 0, c);
                if (a)
                    try {
                        f = String.fromCharCode.apply(null, new Uint8Array(f))
                    } catch (t) {
                        var h = new Uint8Array(f);
                        f = "";
                        for (var u = 0; u < h.length; u++)
                            f += String.fromCharCode(h[u])
                    }
                i.push(f),
                o = p(o, c)
            }
            var l = i.length;
            i.forEach(function(t, o) {
                r(e.decodePacket(t, n, !0), o, l)
            })
        }
    }
    , function(t, e, n) {
        (function(r) {
            function o() {
                return !("undefined" == typeof window || !window.process || "renderer" !== window.process.type) || ("undefined" == typeof navigator || !navigator.userAgent || !navigator.userAgent.toLowerCase().match(/(edge|trident)\/(\d+)/)) && ("undefined" != typeof document && document.documentElement && document.documentElement.style && document.documentElement.style.WebkitAppearance || "undefined" != typeof window && window.console && (window.console.firebug || window.console.exception && window.console.table) || "undefined" != typeof navigator && navigator.userAgent && navigator.userAgent.toLowerCase().match(/firefox\/(\d+)/) && parseInt(RegExp.$1, 10) >= 31 || "undefined" != typeof navigator && navigator.userAgent && navigator.userAgent.toLowerCase().match(/applewebkit\/(\d+)/))
            }
            function i(t) {
                var n = this.useColors;
                if (t[0] = (n ? "%c" : "") + this.namespace + (n ? " %c" : " ") + t[0] + (n ? "%c " : " ") + "+" + e.humanize(this.diff),
                n) {
                    var r = "color: " + this.color;
                    t.splice(1, 0, r, "color: inherit");
                    var o = 0
                      , i = 0;
                    t[0].replace(/%[a-zA-Z%]/g, function(t) {
                        "%%" !== t && (o++,
                        "%c" === t && (i = o))
                    }),
                    t.splice(i, 0, r)
                }
            }
            function s() {
                return "object" == typeof console && console.log && Function.prototype.apply.call(console.log, console, arguments)
            }
            function a(t) {
                try {
                    null == t ? e.storage.removeItem("debug") : e.storage.debug = t
                } catch (t) {}
            }
            function c() {
                var t;
                try {
                    t = e.storage.debug
                } catch (t) {}
                return !t && void 0 !== r && "env"in r && (t = r.env.DEBUG),
                t
            }
            function u() {
                try {
                    return window.localStorage
                } catch (t) {}
            }
            e = t.exports = n(48),
            e.log = s,
            e.formatArgs = i,
            e.save = a,
            e.load = c,
            e.useColors = o,
            e.storage = "undefined" != typeof chrome && void 0 !== chrome.storage ? chrome.storage.local : u(),
            e.colors = ["#0000CC", "#0000FF", "#0033CC", "#0033FF", "#0066CC", "#0066FF", "#0099CC", "#0099FF", "#00CC00", "#00CC33", "#00CC66", "#00CC99", "#00CCCC", "#00CCFF", "#3300CC", "#3300FF", "#3333CC", "#3333FF", "#3366CC", "#3366FF", "#3399CC", "#3399FF", "#33CC00", "#33CC33", "#33CC66", "#33CC99", "#33CCCC", "#33CCFF", "#6600CC", "#6600FF", "#6633CC", "#6633FF", "#66CC00", "#66CC33", "#9900CC", "#9900FF", "#9933CC", "#9933FF", "#99CC00", "#99CC33", "#CC0000", "#CC0033", "#CC0066", "#CC0099", "#CC00CC", "#CC00FF", "#CC3300", "#CC3333", "#CC3366", "#CC3399", "#CC33CC", "#CC33FF", "#CC6600", "#CC6633", "#CC9900", "#CC9933", "#CCCC00", "#CCCC33", "#FF0000", "#FF0033", "#FF0066", "#FF0099", "#FF00CC", "#FF00FF", "#FF3300", "#FF3333", "#FF3366", "#FF3399", "#FF33CC", "#FF33FF", "#FF6600", "#FF6633", "#FF9900", "#FF9933", "#FFCC00", "#FFCC33"],
            e.formatters.j = function(t) {
                try {
                    return JSON.stringify(t)
                } catch (t) {
                    return "[UnexpectedJSONParseError]: " + t.message
                }
            }
            ,
            e.enable(c())
        }
        ).call(e, n(6))
    }
    , function(t, e) {
        t.exports = function(t, e) {
            var n = function() {};
            n.prototype = e.prototype,
            t.prototype = new n,
            t.prototype.constructor = t
        }
    }
    , function(t, e, n) {
        (function(r) {
            function o() {
                return !("undefined" == typeof window || !window.process || "renderer" !== window.process.type) || ("undefined" == typeof navigator || !navigator.userAgent || !navigator.userAgent.toLowerCase().match(/(edge|trident)\/(\d+)/)) && ("undefined" != typeof document && document.documentElement && document.documentElement.style && document.documentElement.style.WebkitAppearance || "undefined" != typeof window && window.console && (window.console.firebug || window.console.exception && window.console.table) || "undefined" != typeof navigator && navigator.userAgent && navigator.userAgent.toLowerCase().match(/firefox\/(\d+)/) && parseInt(RegExp.$1, 10) >= 31 || "undefined" != typeof navigator && navigator.userAgent && navigator.userAgent.toLowerCase().match(/applewebkit\/(\d+)/))
            }
            function i(t) {
                var n = this.useColors;
                if (t[0] = (n ? "%c" : "") + this.namespace + (n ? " %c" : " ") + t[0] + (n ? "%c " : " ") + "+" + e.humanize(this.diff),
                n) {
                    var r = "color: " + this.color;
                    t.splice(1, 0, r, "color: inherit");
                    var o = 0
                      , i = 0;
                    t[0].replace(/%[a-zA-Z%]/g, function(t) {
                        "%%" !== t && (o++,
                        "%c" === t && (i = o))
                    }),
                    t.splice(i, 0, r)
                }
            }
            function s() {
                return "object" == typeof console && console.log && Function.prototype.apply.call(console.log, console, arguments)
            }
            function a(t) {
                try {
                    null == t ? e.storage.removeItem("debug") : e.storage.debug = t
                } catch (t) {}
            }
            function c() {
                var t;
                try {
                    t = e.storage.debug
                } catch (t) {}
                return !t && void 0 !== r && "env"in r && (t = r.env.DEBUG),
                t
            }
            function u() {
                try {
                    return window.localStorage
                } catch (t) {}
            }
            e = t.exports = n(39),
            e.log = s,
            e.formatArgs = i,
            e.save = a,
            e.load = c,
            e.useColors = o,
            e.storage = "undefined" != typeof chrome && void 0 !== chrome.storage ? chrome.storage.local : u(),
            e.colors = ["#0000CC", "#0000FF", "#0033CC", "#0033FF", "#0066CC", "#0066FF", "#0099CC", "#0099FF", "#00CC00", "#00CC33", "#00CC66", "#00CC99", "#00CCCC", "#00CCFF", "#3300CC", "#3300FF", "#3333CC", "#3333FF", "#3366CC", "#3366FF", "#3399CC", "#3399FF", "#33CC00", "#33CC33", "#33CC66", "#33CC99", "#33CCCC", "#33CCFF", "#6600CC", "#6600FF", "#6633CC", "#6633FF", "#66CC00", "#66CC33", "#9900CC", "#9900FF", "#9933CC", "#9933FF", "#99CC00", "#99CC33", "#CC0000", "#CC0033", "#CC0066", "#CC0099", "#CC00CC", "#CC00FF", "#CC3300", "#CC3333", "#CC3366", "#CC3399", "#CC33CC", "#CC33FF", "#CC6600", "#CC6633", "#CC9900", "#CC9933", "#CCCC00", "#CCCC33", "#FF0000", "#FF0033", "#FF0066", "#FF0099", "#FF00CC", "#FF00FF", "#FF3300", "#FF3333", "#FF3366", "#FF3399", "#FF33CC", "#FF33FF", "#FF6600", "#FF6633", "#FF9900", "#FF9933", "#FFCC00", "#FFCC33"],
            e.formatters.j = function(t) {
                try {
                    return JSON.stringify(t)
                } catch (t) {
                    return "[UnexpectedJSONParseError]: " + t.message
                }
            }
            ,
            e.enable(c())
        }
        ).call(e, n(6))
    }
    , function(t, e) {
        e.encode = function(t) {
            var e = "";
            for (var n in t)
                t.hasOwnProperty(n) && (e.length && (e += "&"),
                e += encodeURIComponent(n) + "=" + encodeURIComponent(t[n]));
            return e
        }
        ,
        e.decode = function(t) {
            for (var e = {}, n = t.split("&"), r = 0, o = n.length; r < o; r++) {
                var i = n[r].split("=");
                e[decodeURIComponent(i[0])] = decodeURIComponent(i[1])
            }
            return e
        }
    }
    , function(t, e) {
        function n() {
            throw new Error("setTimeout has not been defined")
        }
        function r() {
            throw new Error("clearTimeout has not been defined")
        }
        function o(t) {
            if (f === setTimeout)
                return setTimeout(t, 0);
            if ((f === n || !f) && setTimeout)
                return f = setTimeout,
                setTimeout(t, 0);
            try {
                return f(t, 0)
            } catch (e) {
                try {
                    return f.call(null, t, 0)
                } catch (e) {
                    return f.call(this, t, 0)
                }
            }
        }
        function i(t) {
            if (h === clearTimeout)
                return clearTimeout(t);
            if ((h === r || !h) && clearTimeout)
                return h = clearTimeout,
                clearTimeout(t);
            try {
                return h(t)
            } catch (e) {
                try {
                    return h.call(null, t)
                } catch (e) {
                    return h.call(this, t)
                }
            }
        }
        function s() {
            y && l && (y = !1,
            l.length ? d = l.concat(d) : g = -1,
            d.length && a())
        }
        function a() {
            if (!y) {
                var t = o(s);
                y = !0;
                for (var e = d.length; e; ) {
                    for (l = d,
                    d = []; ++g < e; )
                        l && l[g].run();
                    g = -1,
                    e = d.length
                }
                l = null,
                y = !1,
                i(t)
            }
        }
        function c(t, e) {
            this.fun = t,
            this.array = e
        }
        function u() {}
        var f, h, p = t.exports = {};
        !function() {
            try {
                f = "function" == typeof setTimeout ? setTimeout : n
            } catch (t) {
                f = n
            }
            try {
                h = "function" == typeof clearTimeout ? clearTimeout : r
            } catch (t) {
                h = r
            }
        }();
        var l, d = [], y = !1, g = -1;
        p.nextTick = function(t) {
            var e = new Array(arguments.length - 1);
            if (arguments.length > 1)
                for (var n = 1; n < arguments.length; n++)
                    e[n - 1] = arguments[n];
            d.push(new c(t,e)),
            1 !== d.length || y || o(a)
        }
        ,
        c.prototype.run = function() {
            this.fun.apply(null, this.array)
        }
        ,
        p.title = "browser",
        p.browser = !0,
        p.env = {},
        p.argv = [],
        p.version = "",
        p.versions = {},
        p.on = u,
        p.addListener = u,
        p.once = u,
        p.off = u,
        p.removeListener = u,
        p.removeAllListeners = u,
        p.emit = u,
        p.prependListener = u,
        p.prependOnceListener = u,
        p.listeners = function(t) {
            return []
        }
        ,
        p.binding = function(t) {
            throw new Error("process.binding is not supported")
        }
        ,
        p.cwd = function() {
            return "/"
        }
        ,
        p.chdir = function(t) {
            throw new Error("process.chdir is not supported")
        }
        ,
        p.umask = function() {
            return 0
        }
    }
    , function(t, e, n) {
        "use strict";
        (function(t) {
            function r() {
                try {
                    var t = new Uint8Array(1);
                    return t.__proto__ = {
                        __proto__: Uint8Array.prototype,
                        foo: function() {
                            return 42
                        }
                    },
                    42 === t.foo() && "function" == typeof t.subarray && 0 === t.subarray(1, 1).byteLength
                } catch (t) {
                    return !1
                }
            }
            function o() {
                return s.TYPED_ARRAY_SUPPORT ? 2147483647 : 1073741823
            }
            function i(t, e) {
                if (o() < e)
                    throw new RangeError("Invalid typed array length");
                return s.TYPED_ARRAY_SUPPORT ? (t = new Uint8Array(e),
                t.__proto__ = s.prototype) : (null === t && (t = new s(e)),
                t.length = e),
                t
            }
            function s(t, e, n) {
                if (!(s.TYPED_ARRAY_SUPPORT || this instanceof s))
                    return new s(t,e,n);
                if ("number" == typeof t) {
                    if ("string" == typeof e)
                        throw new Error("If encoding is specified then the first argument must be a string");
                    return f(this, t)
                }
                return a(this, t, e, n)
            }
            function a(t, e, n, r) {
                if ("number" == typeof e)
                    throw new TypeError('"value" argument must not be a number');
                return "undefined" != typeof ArrayBuffer && e instanceof ArrayBuffer ? l(t, e, n, r) : "string" == typeof e ? h(t, e, n) : d(t, e)
            }
            function c(t) {
                if ("number" != typeof t)
                    throw new TypeError('"size" argument must be a number');
                if (t < 0)
                    throw new RangeError('"size" argument must not be negative')
            }
            function u(t, e, n, r) {
                return c(e),
                e <= 0 ? i(t, e) : void 0 !== n ? "string" == typeof r ? i(t, e).fill(n, r) : i(t, e).fill(n) : i(t, e)
            }
            function f(t, e) {
                if (c(e),
                t = i(t, e < 0 ? 0 : 0 | y(e)),
                !s.TYPED_ARRAY_SUPPORT)
                    for (var n = 0; n < e; ++n)
                        t[n] = 0;
                return t
            }
            function h(t, e, n) {
                if ("string" == typeof n && "" !== n || (n = "utf8"),
                !s.isEncoding(n))
                    throw new TypeError('"encoding" must be a valid string encoding');
                var r = 0 | m(e, n);
                t = i(t, r);
                var o = t.write(e, n);
                return o !== r && (t = t.slice(0, o)),
                t
            }
            function p(t, e) {
                var n = e.length < 0 ? 0 : 0 | y(e.length);
                t = i(t, n);
                for (var r = 0; r < n; r += 1)
                    t[r] = 255 & e[r];
                return t
            }
            function l(t, e, n, r) {
                if (e.byteLength,
                n < 0 || e.byteLength < n)
                    throw new RangeError("'offset' is out of bounds");
                if (e.byteLength < n + (r || 0))
                    throw new RangeError("'length' is out of bounds");
                return e = void 0 === n && void 0 === r ? new Uint8Array(e) : void 0 === r ? new Uint8Array(e,n) : new Uint8Array(e,n,r),
                s.TYPED_ARRAY_SUPPORT ? (t = e,
                t.__proto__ = s.prototype) : t = p(t, e),
                t
            }
            function d(t, e) {
                if (s.isBuffer(e)) {
                    var n = 0 | y(e.length);
                    return t = i(t, n),
                    0 === t.length ? t : (e.copy(t, 0, 0, n),
                    t)
                }
                if (e) {
                    if ("undefined" != typeof ArrayBuffer && e.buffer instanceof ArrayBuffer || "length"in e)
                        return "number" != typeof e.length || K(e.length) ? i(t, 0) : p(t, e);
                    if ("Buffer" === e.type && Q(e.data))
                        return p(t, e.data)
                }
                throw new TypeError("First argument must be a string, Buffer, ArrayBuffer, Array, or array-like object.")
            }
            function y(t) {
                if (t >= o())
                    throw new RangeError("Attempt to allocate Buffer larger than maximum size: 0x" + o().toString(16) + " bytes");
                return 0 | t
            }
            function g(t) {
                return +t != t && (t = 0),
                s.alloc(+t)
            }
            function m(t, e) {
                if (s.isBuffer(t))
                    return t.length;
                if ("undefined" != typeof ArrayBuffer && "function" == typeof ArrayBuffer.isView && (ArrayBuffer.isView(t) || t instanceof ArrayBuffer))
                    return t.byteLength;
                "string" != typeof t && (t = "" + t);
                var n = t.length;
                if (0 === n)
                    return 0;
                for (var r = !1; ; )
                    switch (e) {
                    case "ascii":
                    case "latin1":
                    case "binary":
                        return n;
                    case "utf8":
                    case "utf-8":
                    case void 0:
                        return X(t).length;
                    case "ucs2":
                    case "ucs-2":
                    case "utf16le":
                    case "utf-16le":
                        return 2 * n;
                    case "hex":
                        return n >>> 1;
                    case "base64":
                        return J(t).length;
                    default:
                        if (r)
                            return X(t).length;
                        e = ("" + e).toLowerCase(),
                        r = !0
                    }
            }
            function v(t, e, n) {
                var r = !1;
                if ((void 0 === e || e < 0) && (e = 0),
                e > this.length)
                    return "";
                if ((void 0 === n || n > this.length) && (n = this.length),
                n <= 0)
                    return "";
                if (n >>>= 0,
                e >>>= 0,
                n <= e)
                    return "";
                for (t || (t = "utf8"); ; )
                    switch (t) {
                    case "hex":
                        return O(this, e, n);
                    case "utf8":
                    case "utf-8":
                        return _(this, e, n);
                    case "ascii":
                        return F(this, e, n);
                    case "latin1":
                    case "binary":
                        return P(this, e, n);
                    case "base64":
                        return R(this, e, n);
                    case "ucs2":
                    case "ucs-2":
                    case "utf16le":
                    case "utf-16le":
                        return L(this, e, n);
                    default:
                        if (r)
                            throw new TypeError("Unknown encoding: " + t);
                        t = (t + "").toLowerCase(),
                        r = !0
                    }
            }
            function b(t, e, n) {
                var r = t[e];
                t[e] = t[n],
                t[n] = r
            }
            function w(t, e, n, r, o) {
                if (0 === t.length)
                    return -1;
                if ("string" == typeof n ? (r = n,
                n = 0) : n > 2147483647 ? n = 2147483647 : n < -2147483648 && (n = -2147483648),
                n = +n,
                isNaN(n) && (n = o ? 0 : t.length - 1),
                n < 0 && (n = t.length + n),
                n >= t.length) {
                    if (o)
                        return -1;
                    n = t.length - 1
                } else if (n < 0) {
                    if (!o)
                        return -1;
                    n = 0
                }
                if ("string" == typeof e && (e = s.from(e, r)),
                s.isBuffer(e))
                    return 0 === e.length ? -1 : C(t, e, n, r, o);
                if ("number" == typeof e)
                    return e &= 255,
                    s.TYPED_ARRAY_SUPPORT && "function" == typeof Uint8Array.prototype.indexOf ? o ? Uint8Array.prototype.indexOf.call(t, e, n) : Uint8Array.prototype.lastIndexOf.call(t, e, n) : C(t, [e], n, r, o);
                throw new TypeError("val must be string, number or Buffer")
            }
            function C(t, e, n, r, o) {
                function i(t, e) {
                    return 1 === s ? t[e] : t.readUInt16BE(e * s)
                }
                var s = 1
                  , a = t.length
                  , c = e.length;
                if (void 0 !== r && ("ucs2" === (r = String(r).toLowerCase()) || "ucs-2" === r || "utf16le" === r || "utf-16le" === r)) {
                    if (t.length < 2 || e.length < 2)
                        return -1;
                    s = 2,
                    a /= 2,
                    c /= 2,
                    n /= 2
                }
                var u;
                if (o) {
                    var f = -1;
                    for (u = n; u < a; u++)
                        if (i(t, u) === i(e, -1 === f ? 0 : u - f)) {
                            if (-1 === f && (f = u),
                            u - f + 1 === c)
                                return f * s
                        } else
                            -1 !== f && (u -= u - f),
                            f = -1
                } else
                    for (n + c > a && (n = a - c),
                    u = n; u >= 0; u--) {
                        for (var h = !0, p = 0; p < c; p++)
                            if (i(t, u + p) !== i(e, p)) {
                                h = !1;
                                break
                            }
                        if (h)
                            return u
                    }
                return -1
            }
            function A(t, e, n, r) {
                n = Number(n) || 0;
                var o = t.length - n;
                r ? (r = Number(r)) > o && (r = o) : r = o;
                var i = e.length;
                if (i % 2 != 0)
                    throw new TypeError("Invalid hex string");
                r > i / 2 && (r = i / 2);
                for (var s = 0; s < r; ++s) {
                    var a = parseInt(e.substr(2 * s, 2), 16);
                    if (isNaN(a))
                        return s;
                    t[n + s] = a
                }
                return s
            }
            function E(t, e, n, r) {
                return $(X(e, t.length - n), t, n, r)
            }
            function x(t, e, n, r) {
                return $(W(e), t, n, r)
            }
            function k(t, e, n, r) {
                return x(t, e, n, r)
            }
            function B(t, e, n, r) {
                return $(J(e), t, n, r)
            }
            function T(t, e, n, r) {
                return $(V(e, t.length - n), t, n, r)
            }
            function R(t, e, n) {
                return 0 === e && n === t.length ? G.fromByteArray(t) : G.fromByteArray(t.slice(e, n))
            }
            function _(t, e, n) {
                n = Math.min(t.length, n);
                for (var r = [], o = e; o < n; ) {
                    var i = t[o]
                      , s = null
                      , a = i > 239 ? 4 : i > 223 ? 3 : i > 191 ? 2 : 1;
                    if (o + a <= n) {
                        var c, u, f, h;
                        switch (a) {
                        case 1:
                            i < 128 && (s = i);
                            break;
                        case 2:
                            c = t[o + 1],
                            128 == (192 & c) && (h = (31 & i) << 6 | 63 & c) > 127 && (s = h);
                            break;
                        case 3:
                            c = t[o + 1],
                            u = t[o + 2],
                            128 == (192 & c) && 128 == (192 & u) && (h = (15 & i) << 12 | (63 & c) << 6 | 63 & u) > 2047 && (h < 55296 || h > 57343) && (s = h);
                            break;
                        case 4:
                            c = t[o + 1],
                            u = t[o + 2],
                            f = t[o + 3],
                            128 == (192 & c) && 128 == (192 & u) && 128 == (192 & f) && (h = (15 & i) << 18 | (63 & c) << 12 | (63 & u) << 6 | 63 & f) > 65535 && h < 1114112 && (s = h)
                        }
                    }
                    null === s ? (s = 65533,
                    a = 1) : s > 65535 && (s -= 65536,
                    r.push(s >>> 10 & 1023 | 55296),
                    s = 56320 | 1023 & s),
                    r.push(s),
                    o += a
                }
                return S(r)
            }
            function S(t) {
                var e = t.length;
                if (e <= tt)
                    return String.fromCharCode.apply(String, t);
                for (var n = "", r = 0; r < e; )
                    n += String.fromCharCode.apply(String, t.slice(r, r += tt));
                return n
            }
            function F(t, e, n) {
                var r = "";
                n = Math.min(t.length, n);
                for (var o = e; o < n; ++o)
                    r += String.fromCharCode(127 & t[o]);
                return r
            }
            function P(t, e, n) {
                var r = "";
                n = Math.min(t.length, n);
                for (var o = e; o < n; ++o)
                    r += String.fromCharCode(t[o]);
                return r
            }
            function O(t, e, n) {
                var r = t.length;
                (!e || e < 0) && (e = 0),
                (!n || n < 0 || n > r) && (n = r);
                for (var o = "", i = e; i < n; ++i)
                    o += H(t[i]);
                return o
            }
            function L(t, e, n) {
                for (var r = t.slice(e, n), o = "", i = 0; i < r.length; i += 2)
                    o += String.fromCharCode(r[i] + 256 * r[i + 1]);
                return o
            }
            function U(t, e, n) {
                if (t % 1 != 0 || t < 0)
                    throw new RangeError("offset is not uint");
                if (t + e > n)
                    throw new RangeError("Trying to access beyond buffer length")
            }
            function N(t, e, n, r, o, i) {
                if (!s.isBuffer(t))
                    throw new TypeError('"buffer" argument must be a Buffer instance');
                if (e > o || e < i)
                    throw new RangeError('"value" argument is out of bounds');
                if (n + r > t.length)
                    throw new RangeError("Index out of range")
            }
            function D(t, e, n, r) {
                e < 0 && (e = 65535 + e + 1);
                for (var o = 0, i = Math.min(t.length - n, 2); o < i; ++o)
                    t[n + o] = (e & 255 << 8 * (r ? o : 1 - o)) >>> 8 * (r ? o : 1 - o)
            }
            function I(t, e, n, r) {
                e < 0 && (e = 4294967295 + e + 1);
                for (var o = 0, i = Math.min(t.length - n, 4); o < i; ++o)
                    t[n + o] = e >>> 8 * (r ? o : 3 - o) & 255
            }
            function j(t, e, n, r, o, i) {
                if (n + r > t.length)
                    throw new RangeError("Index out of range");
                if (n < 0)
                    throw new RangeError("Index out of range")
            }
            function M(t, e, n, r, o) {
                return o || j(t, e, n, 4, 3.4028234663852886e38, -3.4028234663852886e38),
                Z.write(t, e, n, r, 23, 4),
                n + 4
            }
            function q(t, e, n, r, o) {
                return o || j(t, e, n, 8, 1.7976931348623157e308, -1.7976931348623157e308),
                Z.write(t, e, n, r, 52, 8),
                n + 8
            }
            function Y(t) {
                if (t = z(t).replace(et, ""),
                t.length < 2)
                    return "";
                for (; t.length % 4 != 0; )
                    t += "=";
                return t
            }
            function z(t) {
                return t.trim ? t.trim() : t.replace(/^\s+|\s+$/g, "")
            }
            function H(t) {
                return t < 16 ? "0" + t.toString(16) : t.toString(16)
            }
            function X(t, e) {
                e = e || 1 / 0;
                for (var n, r = t.length, o = null, i = [], s = 0; s < r; ++s) {
                    if ((n = t.charCodeAt(s)) > 55295 && n < 57344) {
                        if (!o) {
                            if (n > 56319) {
                                (e -= 3) > -1 && i.push(239, 191, 189);
                                continue
                            }
                            if (s + 1 === r) {
                                (e -= 3) > -1 && i.push(239, 191, 189);
                                continue
                            }
                            o = n;
                            continue
                        }
                        if (n < 56320) {
                            (e -= 3) > -1 && i.push(239, 191, 189),
                            o = n;
                            continue
                        }
                        n = 65536 + (o - 55296 << 10 | n - 56320)
                    } else
                        o && (e -= 3) > -1 && i.push(239, 191, 189);
                    if (o = null,
                    n < 128) {
                        if ((e -= 1) < 0)
                            break;
                        i.push(n)
                    } else if (n < 2048) {
                        if ((e -= 2) < 0)
                            break;
                        i.push(n >> 6 | 192, 63 & n | 128)
                    } else if (n < 65536) {
                        if ((e -= 3) < 0)
                            break;
                        i.push(n >> 12 | 224, n >> 6 & 63 | 128, 63 & n | 128)
                    } else {
                        if (!(n < 1114112))
                            throw new Error("Invalid code point");
                        if ((e -= 4) < 0)
                            break;
                        i.push(n >> 18 | 240, n >> 12 & 63 | 128, n >> 6 & 63 | 128, 63 & n | 128)
                    }
                }
                return i
            }
            function W(t) {
                for (var e = [], n = 0; n < t.length; ++n)
                    e.push(255 & t.charCodeAt(n));
                return e
            }
            function V(t, e) {
                for (var n, r, o, i = [], s = 0; s < t.length && !((e -= 2) < 0); ++s)
                    n = t.charCodeAt(s),
                    r = n >> 8,
                    o = n % 256,
                    i.push(o),
                    i.push(r);
                return i
            }
            function J(t) {
                return G.toByteArray(Y(t))
            }
            function $(t, e, n, r) {
                for (var o = 0; o < r && !(o + n >= e.length || o >= t.length); ++o)
                    e[o + n] = t[o];
                return o
            }
            function K(t) {
                return t !== t
            }
            /*!
 * The buffer module from node.js, for the browser.
 *
 * @author   Feross Aboukhadijeh <http://feross.org>
 * @license  MIT
 */
            var G = n(32)
              , Z = n(45)
              , Q = n(46);
            e.Buffer = s,
            e.SlowBuffer = g,
            e.INSPECT_MAX_BYTES = 50,
            s.TYPED_ARRAY_SUPPORT = void 0 !== t.TYPED_ARRAY_SUPPORT ? t.TYPED_ARRAY_SUPPORT : r(),
            e.kMaxLength = o(),
            s.poolSize = 8192,
            s._augment = function(t) {
                return t.__proto__ = s.prototype,
                t
            }
            ,
            s.from = function(t, e, n) {
                return a(null, t, e, n)
            }
            ,
            s.TYPED_ARRAY_SUPPORT && (s.prototype.__proto__ = Uint8Array.prototype,
            s.__proto__ = Uint8Array,
            "undefined" != typeof Symbol && Symbol.species && s[Symbol.species] === s && Object.defineProperty(s, Symbol.species, {
                value: null,
                configurable: !0
            })),
            s.alloc = function(t, e, n) {
                return u(null, t, e, n)
            }
            ,
            s.allocUnsafe = function(t) {
                return f(null, t)
            }
            ,
            s.allocUnsafeSlow = function(t) {
                return f(null, t)
            }
            ,
            s.isBuffer = function(t) {
                return !(null == t || !t._isBuffer)
            }
            ,
            s.compare = function(t, e) {
                if (!s.isBuffer(t) || !s.isBuffer(e))
                    throw new TypeError("Arguments must be Buffers");
                if (t === e)
                    return 0;
                for (var n = t.length, r = e.length, o = 0, i = Math.min(n, r); o < i; ++o)
                    if (t[o] !== e[o]) {
                        n = t[o],
                        r = e[o];
                        break
                    }
                return n < r ? -1 : r < n ? 1 : 0
            }
            ,
            s.isEncoding = function(t) {
                switch (String(t).toLowerCase()) {
                case "hex":
                case "utf8":
                case "utf-8":
                case "ascii":
                case "latin1":
                case "binary":
                case "base64":
                case "ucs2":
                case "ucs-2":
                case "utf16le":
                case "utf-16le":
                    return !0;
                default:
                    return !1
                }
            }
            ,
            s.concat = function(t, e) {
                if (!Q(t))
                    throw new TypeError('"list" argument must be an Array of Buffers');
                if (0 === t.length)
                    return s.alloc(0);
                var n;
                if (void 0 === e)
                    for (e = 0,
                    n = 0; n < t.length; ++n)
                        e += t[n].length;
                var r = s.allocUnsafe(e)
                  , o = 0;
                for (n = 0; n < t.length; ++n) {
                    var i = t[n];
                    if (!s.isBuffer(i))
                        throw new TypeError('"list" argument must be an Array of Buffers');
                    i.copy(r, o),
                    o += i.length
                }
                return r
            }
            ,
            s.byteLength = m,
            s.prototype._isBuffer = !0,
            s.prototype.swap16 = function() {
                var t = this.length;
                if (t % 2 != 0)
                    throw new RangeError("Buffer size must be a multiple of 16-bits");
                for (var e = 0; e < t; e += 2)
                    b(this, e, e + 1);
                return this
            }
            ,
            s.prototype.swap32 = function() {
                var t = this.length;
                if (t % 4 != 0)
                    throw new RangeError("Buffer size must be a multiple of 32-bits");
                for (var e = 0; e < t; e += 4)
                    b(this, e, e + 3),
                    b(this, e + 1, e + 2);
                return this
            }
            ,
            s.prototype.swap64 = function() {
                var t = this.length;
                if (t % 8 != 0)
                    throw new RangeError("Buffer size must be a multiple of 64-bits");
                for (var e = 0; e < t; e += 8)
                    b(this, e, e + 7),
                    b(this, e + 1, e + 6),
                    b(this, e + 2, e + 5),
                    b(this, e + 3, e + 4);
                return this
            }
            ,
            s.prototype.toString = function() {
                var t = 0 | this.length;
                return 0 === t ? "" : 0 === arguments.length ? _(this, 0, t) : v.apply(this, arguments)
            }
            ,
            s.prototype.equals = function(t) {
                if (!s.isBuffer(t))
                    throw new TypeError("Argument must be a Buffer");
                return this === t || 0 === s.compare(this, t)
            }
            ,
            s.prototype.inspect = function() {
                var t = ""
                  , n = e.INSPECT_MAX_BYTES;
                return this.length > 0 && (t = this.toString("hex", 0, n).match(/.{2}/g).join(" "),
                this.length > n && (t += " ... ")),
                "<Buffer " + t + ">"
            }
            ,
            s.prototype.compare = function(t, e, n, r, o) {
                if (!s.isBuffer(t))
                    throw new TypeError("Argument must be a Buffer");
                if (void 0 === e && (e = 0),
                void 0 === n && (n = t ? t.length : 0),
                void 0 === r && (r = 0),
                void 0 === o && (o = this.length),
                e < 0 || n > t.length || r < 0 || o > this.length)
                    throw new RangeError("out of range index");
                if (r >= o && e >= n)
                    return 0;
                if (r >= o)
                    return -1;
                if (e >= n)
                    return 1;
                if (e >>>= 0,
                n >>>= 0,
                r >>>= 0,
                o >>>= 0,
                this === t)
                    return 0;
                for (var i = o - r, a = n - e, c = Math.min(i, a), u = this.slice(r, o), f = t.slice(e, n), h = 0; h < c; ++h)
                    if (u[h] !== f[h]) {
                        i = u[h],
                        a = f[h];
                        break
                    }
                return i < a ? -1 : a < i ? 1 : 0
            }
            ,
            s.prototype.includes = function(t, e, n) {
                return -1 !== this.indexOf(t, e, n)
            }
            ,
            s.prototype.indexOf = function(t, e, n) {
                return w(this, t, e, n, !0)
            }
            ,
            s.prototype.lastIndexOf = function(t, e, n) {
                return w(this, t, e, n, !1)
            }
            ,
            s.prototype.write = function(t, e, n, r) {
                if (void 0 === e)
                    r = "utf8",
                    n = this.length,
                    e = 0;
                else if (void 0 === n && "string" == typeof e)
                    r = e,
                    n = this.length,
                    e = 0;
                else {
                    if (!isFinite(e))
                        throw new Error("Buffer.write(string, encoding, offset[, length]) is no longer supported");
                    e |= 0,
                    isFinite(n) ? (n |= 0,
                    void 0 === r && (r = "utf8")) : (r = n,
                    n = void 0)
                }
                var o = this.length - e;
                if ((void 0 === n || n > o) && (n = o),
                t.length > 0 && (n < 0 || e < 0) || e > this.length)
                    throw new RangeError("Attempt to write outside buffer bounds");
                r || (r = "utf8");
                for (var i = !1; ; )
                    switch (r) {
                    case "hex":
                        return A(this, t, e, n);
                    case "utf8":
                    case "utf-8":
                        return E(this, t, e, n);
                    case "ascii":
                        return x(this, t, e, n);
                    case "latin1":
                    case "binary":
                        return k(this, t, e, n);
                    case "base64":
                        return B(this, t, e, n);
                    case "ucs2":
                    case "ucs-2":
                    case "utf16le":
                    case "utf-16le":
                        return T(this, t, e, n);
                    default:
                        if (i)
                            throw new TypeError("Unknown encoding: " + r);
                        r = ("" + r).toLowerCase(),
                        i = !0
                    }
            }
            ,
            s.prototype.toJSON = function() {
                return {
                    type: "Buffer",
                    data: Array.prototype.slice.call(this._arr || this, 0)
                }
            }
            ;
            var tt = 4096;
            s.prototype.slice = function(t, e) {
                var n = this.length;
                t = ~~t,
                e = void 0 === e ? n : ~~e,
                t < 0 ? (t += n) < 0 && (t = 0) : t > n && (t = n),
                e < 0 ? (e += n) < 0 && (e = 0) : e > n && (e = n),
                e < t && (e = t);
                var r;
                if (s.TYPED_ARRAY_SUPPORT)
                    r = this.subarray(t, e),
                    r.__proto__ = s.prototype;
                else {
                    var o = e - t;
                    r = new s(o,void 0);
                    for (var i = 0; i < o; ++i)
                        r[i] = this[i + t]
                }
                return r
            }
            ,
            s.prototype.readUIntLE = function(t, e, n) {
                t |= 0,
                e |= 0,
                n || U(t, e, this.length);
                for (var r = this[t], o = 1, i = 0; ++i < e && (o *= 256); )
                    r += this[t + i] * o;
                return r
            }
            ,
            s.prototype.readUIntBE = function(t, e, n) {
                t |= 0,
                e |= 0,
                n || U(t, e, this.length);
                for (var r = this[t + --e], o = 1; e > 0 && (o *= 256); )
                    r += this[t + --e] * o;
                return r
            }
            ,
            s.prototype.readUInt8 = function(t, e) {
                return e || U(t, 1, this.length),
                this[t]
            }
            ,
            s.prototype.readUInt16LE = function(t, e) {
                return e || U(t, 2, this.length),
                this[t] | this[t + 1] << 8
            }
            ,
            s.prototype.readUInt16BE = function(t, e) {
                return e || U(t, 2, this.length),
                this[t] << 8 | this[t + 1]
            }
            ,
            s.prototype.readUInt32LE = function(t, e) {
                return e || U(t, 4, this.length),
                (this[t] | this[t + 1] << 8 | this[t + 2] << 16) + 16777216 * this[t + 3]
            }
            ,
            s.prototype.readUInt32BE = function(t, e) {
                return e || U(t, 4, this.length),
                16777216 * this[t] + (this[t + 1] << 16 | this[t + 2] << 8 | this[t + 3])
            }
            ,
            s.prototype.readIntLE = function(t, e, n) {
                t |= 0,
                e |= 0,
                n || U(t, e, this.length);
                for (var r = this[t], o = 1, i = 0; ++i < e && (o *= 256); )
                    r += this[t + i] * o;
                return o *= 128,
                r >= o && (r -= Math.pow(2, 8 * e)),
                r
            }
            ,
            s.prototype.readIntBE = function(t, e, n) {
                t |= 0,
                e |= 0,
                n || U(t, e, this.length);
                for (var r = e, o = 1, i = this[t + --r]; r > 0 && (o *= 256); )
                    i += this[t + --r] * o;
                return o *= 128,
                i >= o && (i -= Math.pow(2, 8 * e)),
                i
            }
            ,
            s.prototype.readInt8 = function(t, e) {
                return e || U(t, 1, this.length),
                128 & this[t] ? -1 * (255 - this[t] + 1) : this[t]
            }
            ,
            s.prototype.readInt16LE = function(t, e) {
                e || U(t, 2, this.length);
                var n = this[t] | this[t + 1] << 8;
                return 32768 & n ? 4294901760 | n : n
            }
            ,
            s.prototype.readInt16BE = function(t, e) {
                e || U(t, 2, this.length);
                var n = this[t + 1] | this[t] << 8;
                return 32768 & n ? 4294901760 | n : n
            }
            ,
            s.prototype.readInt32LE = function(t, e) {
                return e || U(t, 4, this.length),
                this[t] | this[t + 1] << 8 | this[t + 2] << 16 | this[t + 3] << 24
            }
            ,
            s.prototype.readInt32BE = function(t, e) {
                return e || U(t, 4, this.length),
                this[t] << 24 | this[t + 1] << 16 | this[t + 2] << 8 | this[t + 3]
            }
            ,
            s.prototype.readFloatLE = function(t, e) {
                return e || U(t, 4, this.length),
                Z.read(this, t, !0, 23, 4)
            }
            ,
            s.prototype.readFloatBE = function(t, e) {
                return e || U(t, 4, this.length),
                Z.read(this, t, !1, 23, 4)
            }
            ,
            s.prototype.readDoubleLE = function(t, e) {
                return e || U(t, 8, this.length),
                Z.read(this, t, !0, 52, 8)
            }
            ,
            s.prototype.readDoubleBE = function(t, e) {
                return e || U(t, 8, this.length),
                Z.read(this, t, !1, 52, 8)
            }
            ,
            s.prototype.writeUIntLE = function(t, e, n, r) {
                if (t = +t,
                e |= 0,
                n |= 0,
                !r) {
                    N(this, t, e, n, Math.pow(2, 8 * n) - 1, 0)
                }
                var o = 1
                  , i = 0;
                for (this[e] = 255 & t; ++i < n && (o *= 256); )
                    this[e + i] = t / o & 255;
                return e + n
            }
            ,
            s.prototype.writeUIntBE = function(t, e, n, r) {
                if (t = +t,
                e |= 0,
                n |= 0,
                !r) {
                    N(this, t, e, n, Math.pow(2, 8 * n) - 1, 0)
                }
                var o = n - 1
                  , i = 1;
                for (this[e + o] = 255 & t; --o >= 0 && (i *= 256); )
                    this[e + o] = t / i & 255;
                return e + n
            }
            ,
            s.prototype.writeUInt8 = function(t, e, n) {
                return t = +t,
                e |= 0,
                n || N(this, t, e, 1, 255, 0),
                s.TYPED_ARRAY_SUPPORT || (t = Math.floor(t)),
                this[e] = 255 & t,
                e + 1
            }
            ,
            s.prototype.writeUInt16LE = function(t, e, n) {
                return t = +t,
                e |= 0,
                n || N(this, t, e, 2, 65535, 0),
                s.TYPED_ARRAY_SUPPORT ? (this[e] = 255 & t,
                this[e + 1] = t >>> 8) : D(this, t, e, !0),
                e + 2
            }
            ,
            s.prototype.writeUInt16BE = function(t, e, n) {
                return t = +t,
                e |= 0,
                n || N(this, t, e, 2, 65535, 0),
                s.TYPED_ARRAY_SUPPORT ? (this[e] = t >>> 8,
                this[e + 1] = 255 & t) : D(this, t, e, !1),
                e + 2
            }
            ,
            s.prototype.writeUInt32LE = function(t, e, n) {
                return t = +t,
                e |= 0,
                n || N(this, t, e, 4, 4294967295, 0),
                s.TYPED_ARRAY_SUPPORT ? (this[e + 3] = t >>> 24,
                this[e + 2] = t >>> 16,
                this[e + 1] = t >>> 8,
                this[e] = 255 & t) : I(this, t, e, !0),
                e + 4
            }
            ,
            s.prototype.writeUInt32BE = function(t, e, n) {
                return t = +t,
                e |= 0,
                n || N(this, t, e, 4, 4294967295, 0),
                s.TYPED_ARRAY_SUPPORT ? (this[e] = t >>> 24,
                this[e + 1] = t >>> 16,
                this[e + 2] = t >>> 8,
                this[e + 3] = 255 & t) : I(this, t, e, !1),
                e + 4
            }
            ,
            s.prototype.writeIntLE = function(t, e, n, r) {
                if (t = +t,
                e |= 0,
                !r) {
                    var o = Math.pow(2, 8 * n - 1);
                    N(this, t, e, n, o - 1, -o)
                }
                var i = 0
                  , s = 1
                  , a = 0;
                for (this[e] = 255 & t; ++i < n && (s *= 256); )
                    t < 0 && 0 === a && 0 !== this[e + i - 1] && (a = 1),
                    this[e + i] = (t / s >> 0) - a & 255;
                return e + n
            }
            ,
            s.prototype.writeIntBE = function(t, e, n, r) {
                if (t = +t,
                e |= 0,
                !r) {
                    var o = Math.pow(2, 8 * n - 1);
                    N(this, t, e, n, o - 1, -o)
                }
                var i = n - 1
                  , s = 1
                  , a = 0;
                for (this[e + i] = 255 & t; --i >= 0 && (s *= 256); )
                    t < 0 && 0 === a && 0 !== this[e + i + 1] && (a = 1),
                    this[e + i] = (t / s >> 0) - a & 255;
                return e + n
            }
            ,
            s.prototype.writeInt8 = function(t, e, n) {
                return t = +t,
                e |= 0,
                n || N(this, t, e, 1, 127, -128),
                s.TYPED_ARRAY_SUPPORT || (t = Math.floor(t)),
                t < 0 && (t = 255 + t + 1),
                this[e] = 255 & t,
                e + 1
            }
            ,
            s.prototype.writeInt16LE = function(t, e, n) {
                return t = +t,
                e |= 0,
                n || N(this, t, e, 2, 32767, -32768),
                s.TYPED_ARRAY_SUPPORT ? (this[e] = 255 & t,
                this[e + 1] = t >>> 8) : D(this, t, e, !0),
                e + 2
            }
            ,
            s.prototype.writeInt16BE = function(t, e, n) {
                return t = +t,
                e |= 0,
                n || N(this, t, e, 2, 32767, -32768),
                s.TYPED_ARRAY_SUPPORT ? (this[e] = t >>> 8,
                this[e + 1] = 255 & t) : D(this, t, e, !1),
                e + 2
            }
            ,
            s.prototype.writeInt32LE = function(t, e, n) {
                return t = +t,
                e |= 0,
                n || N(this, t, e, 4, 2147483647, -2147483648),
                s.TYPED_ARRAY_SUPPORT ? (this[e] = 255 & t,
                this[e + 1] = t >>> 8,
                this[e + 2] = t >>> 16,
                this[e + 3] = t >>> 24) : I(this, t, e, !0),
                e + 4
            }
            ,
            s.prototype.writeInt32BE = function(t, e, n) {
                return t = +t,
                e |= 0,
                n || N(this, t, e, 4, 2147483647, -2147483648),
                t < 0 && (t = 4294967295 + t + 1),
                s.TYPED_ARRAY_SUPPORT ? (this[e] = t >>> 24,
                this[e + 1] = t >>> 16,
                this[e + 2] = t >>> 8,
                this[e + 3] = 255 & t) : I(this, t, e, !1),
                e + 4
            }
            ,
            s.prototype.writeFloatLE = function(t, e, n) {
                return M(this, t, e, !0, n)
            }
            ,
            s.prototype.writeFloatBE = function(t, e, n) {
                return M(this, t, e, !1, n)
            }
            ,
            s.prototype.writeDoubleLE = function(t, e, n) {
                return q(this, t, e, !0, n)
            }
            ,
            s.prototype.writeDoubleBE = function(t, e, n) {
                return q(this, t, e, !1, n)
            }
            ,
            s.prototype.copy = function(t, e, n, r) {
                if (n || (n = 0),
                r || 0 === r || (r = this.length),
                e >= t.length && (e = t.length),
                e || (e = 0),
                r > 0 && r < n && (r = n),
                r === n)
                    return 0;
                if (0 === t.length || 0 === this.length)
                    return 0;
                if (e < 0)
                    throw new RangeError("targetStart out of bounds");
                if (n < 0 || n >= this.length)
                    throw new RangeError("sourceStart out of bounds");
                if (r < 0)
                    throw new RangeError("sourceEnd out of bounds");
                r > this.length && (r = this.length),
                t.length - e < r - n && (r = t.length - e + n);
                var o, i = r - n;
                if (this === t && n < e && e < r)
                    for (o = i - 1; o >= 0; --o)
                        t[o + e] = this[o + n];
                else if (i < 1e3 || !s.TYPED_ARRAY_SUPPORT)
                    for (o = 0; o < i; ++o)
                        t[o + e] = this[o + n];
                else
                    Uint8Array.prototype.set.call(t, this.subarray(n, n + i), e);
                return i
            }
            ,
            s.prototype.fill = function(t, e, n, r) {
                if ("string" == typeof t) {
                    if ("string" == typeof e ? (r = e,
                    e = 0,
                    n = this.length) : "string" == typeof n && (r = n,
                    n = this.length),
                    1 === t.length) {
                        var o = t.charCodeAt(0);
                        o < 256 && (t = o)
                    }
                    if (void 0 !== r && "string" != typeof r)
                        throw new TypeError("encoding must be a string");
                    if ("string" == typeof r && !s.isEncoding(r))
                        throw new TypeError("Unknown encoding: " + r)
                } else
                    "number" == typeof t && (t &= 255);
                if (e < 0 || this.length < e || this.length < n)
                    throw new RangeError("Out of range index");
                if (n <= e)
                    return this;
                e >>>= 0,
                n = void 0 === n ? this.length : n >>> 0,
                t || (t = 0);
                var i;
                if ("number" == typeof t)
                    for (i = e; i < n; ++i)
                        this[i] = t;
                else {
                    var a = s.isBuffer(t) ? t : X(new s(t,r).toString())
                      , c = a.length;
                    for (i = 0; i < n - e; ++i)
                        this[i + e] = a[i % c]
                }
                return this
            }
            ;
            var et = /[^+\/0-9A-Za-z-_]/g
        }
        ).call(e, n(52))
    }
    , function(t, e) {
        t.exports = function() {
            return "undefined" != typeof self ? self : "undefined" != typeof window ? window : Function("return this")()
        }()
    }
    , function(t, e, n) {
        function r(t) {
            this.path = t.path,
            this.hostname = t.hostname,
            this.port = t.port,
            this.secure = t.secure,
            this.query = t.query,
            this.timestampParam = t.timestampParam,
            this.timestampRequests = t.timestampRequests,
            this.readyState = "",
            this.agent = t.agent || !1,
            this.socket = t.socket,
            this.enablesXDR = t.enablesXDR,
            this.withCredentials = t.withCredentials,
            this.pfx = t.pfx,
            this.key = t.key,
            this.passphrase = t.passphrase,
            this.cert = t.cert,
            this.ca = t.ca,
            this.ciphers = t.ciphers,
            this.rejectUnauthorized = t.rejectUnauthorized,
            this.forceNode = t.forceNode,
            this.isReactNative = t.isReactNative,
            this.extraHeaders = t.extraHeaders,
            this.localAddress = t.localAddress
        }
        var o = n(1)
          , i = n(0);
        t.exports = r,
        i(r.prototype),
        r.prototype.onError = function(t, e) {
            var n = new Error(t);
            return n.type = "TransportError",
            n.description = e,
            this.emit("error", n),
            this
        }
        ,
        r.prototype.open = function() {
            return "closed" !== this.readyState && "" !== this.readyState || (this.readyState = "opening",
            this.doOpen()),
            this
        }
        ,
        r.prototype.close = function() {
            return "opening" !== this.readyState && "open" !== this.readyState || (this.doClose(),
            this.onClose()),
            this
        }
        ,
        r.prototype.send = function(t) {
            if ("open" !== this.readyState)
                throw new Error("Transport not open");
            this.write(t)
        }
        ,
        r.prototype.onOpen = function() {
            this.readyState = "open",
            this.writable = !0,
            this.emit("open")
        }
        ,
        r.prototype.onData = function(t) {
            var e = o.decodePacket(t, this.socket.binaryType);
            this.onPacket(e)
        }
        ,
        r.prototype.onPacket = function(t) {
            this.emit("packet", t)
        }
        ,
        r.prototype.onClose = function() {
            this.readyState = "closed",
            this.emit("close")
        }
    }
    , function(t, e, n) {
        var r = n(44)
          , o = n(8);
        t.exports = function(t) {
            var e = t.xdomain
              , n = t.xscheme
              , i = t.enablesXDR;
            try {
                if ("undefined" != typeof XMLHttpRequest && (!e || r))
                    return new XMLHttpRequest
            } catch (t) {}
            try {
                if ("undefined" != typeof XDomainRequest && !n && i)
                    return new XDomainRequest
            } catch (t) {}
            if (!e)
                try {
                    return new (o[["Active"].concat("Object").join("X")])("Microsoft.XMLHTTP")
                } catch (t) {}
        }
    }
    , function(t, e, n) {
        function r() {}
        function o(t) {
            var n = "" + t.type;
            if (e.BINARY_EVENT !== t.type && e.BINARY_ACK !== t.type || (n += t.attachments + "-"),
            t.nsp && "/" !== t.nsp && (n += t.nsp + ","),
            null != t.id && (n += t.id),
            null != t.data) {
                var r = i(t.data);
                if (!1 === r)
                    return v;
                n += r
            }
            return l("encoded %j as %s", t, n),
            n
        }
        function i(t) {
            try {
                return JSON.stringify(t)
            } catch (t) {
                return !1
            }
        }
        function s(t, e) {
            function n(t) {
                var n = y.deconstructPacket(t)
                  , r = o(n.packet)
                  , i = n.buffers;
                i.unshift(r),
                e(i)
            }
            y.removeBlobs(t, n)
        }
        function a() {
            this.reconstructor = null
        }
        function c(t, e) {
            switch (t) {
            case 0:
                return "object" == typeof e;
            case 1:
                return void 0 === e;
            case 4:
                return "string" == typeof e || "object" == typeof e;
            case 2:
            case 5:
                return g(e) && ("string" == typeof e[0] || "number" == typeof e[0]);
            case 3:
            case 6:
                return g(e)
            }
        }
        function u(t) {
            var n = 0
              , r = {
                type: Number(t.charAt(0))
            };
            if (null == e.types[r.type])
                return p("unknown packet type " + r.type);
            if (e.BINARY_EVENT === r.type || e.BINARY_ACK === r.type) {
                for (var o = n + 1; "-" !== t.charAt(++n) && n != t.length; )
                    ;
                var i = t.substring(o, n);
                if (i != Number(i) || "-" !== t.charAt(n))
                    throw new Error("Illegal attachments");
                r.attachments = Number(i)
            }
            if ("/" === t.charAt(n + 1)) {
                for (var o = n + 1; ++n; ) {
                    var s = t.charAt(n);
                    if ("," === s)
                        break;
                    if (n === t.length)
                        break
                }
                r.nsp = t.substring(o, n)
            } else
                r.nsp = "/";
            var a = t.charAt(n + 1);
            if ("" !== a && Number(a) == a) {
                for (var o = n + 1; ++n; ) {
                    var s = t.charAt(n);
                    if (null == s || Number(s) != s) {
                        --n;
                        break
                    }
                    if (n === t.length)
                        break
                }
                r.id = Number(t.substring(o, n + 1))
            }
            if (t.charAt(++n)) {
                var u = f(t.substr(n));
                if (!c(r.type, u))
                    throw new Error("invalid payload");
                r.data = u
            }
            return l("decoded %s as %j", t, r),
            r
        }
        function f(t) {
            try {
                return JSON.parse(t)
            } catch (t) {
                return !1
            }
        }
        function h(t) {
            this.reconPack = t,
            this.buffers = []
        }
        function p(t) {
            return {
                type: e.ERROR,
                data: "parser error: " + t
            }
        }
        var l = n(2)("socket.io-parser")
          , d = n(0)
          , y = n(50)
          , g = n(21)
          , m = n(22);
        e.protocol = 4,
        e.types = ["CONNECT", "DISCONNECT", "EVENT", "ACK", "ERROR", "BINARY_EVENT", "BINARY_ACK"],
        e.CONNECT = 0,
        e.DISCONNECT = 1,
        e.EVENT = 2,
        e.ACK = 3,
        e.ERROR = 4,
        e.BINARY_EVENT = 5,
        e.BINARY_ACK = 6,
        e.Encoder = r,
        e.Decoder = a;
        var v = e.ERROR + '"encode error"';
        r.prototype.encode = function(t, n) {
            if (l("encoding packet %j", t),
            e.BINARY_EVENT === t.type || e.BINARY_ACK === t.type)
                s(t, n);
            else {
                n([o(t)])
            }
        }
        ,
        d(a.prototype),
        a.prototype.add = function(t) {
            var n;
            if ("string" == typeof t) {
                if (this.reconstructor)
                    throw new Error("got plaintext data when reconstructing a packet");
                n = u(t),
                e.BINARY_EVENT === n.type || e.BINARY_ACK === n.type ? (this.reconstructor = new h(n),
                0 === this.reconstructor.reconPack.attachments && this.emit("decoded", n)) : this.emit("decoded", n)
            } else {
                if (!m(t) && !t.base64)
                    throw new Error("Unknown type: " + t);
                if (!this.reconstructor)
                    throw new Error("got binary data when not reconstructing a packet");
                (n = this.reconstructor.takeBinaryData(t)) && (this.reconstructor = null,
                this.emit("decoded", n))
            }
        }
        ,
        a.prototype.destroy = function() {
            this.reconstructor && this.reconstructor.finishedReconstruction()
        }
        ,
        h.prototype.takeBinaryData = function(t) {
            if (this.buffers.push(t),
            this.buffers.length === this.reconPack.attachments) {
                var e = y.reconstructPacket(this.reconPack, this.buffers);
                return this.finishedReconstruction(),
                e
            }
            return null
        }
        ,
        h.prototype.finishedReconstruction = function() {
            this.reconPack = null,
            this.buffers = []
        }
    }
    , function(t, e) {
        var n = [].slice;
        t.exports = function(t, e) {
            if ("string" == typeof e && (e = t[e]),
            "function" != typeof e)
                throw new Error("bind() requires a function");
            var r = n.call(arguments, 2);
            return function() {
                return e.apply(t, r.concat(n.call(arguments)))
            }
        }
    }
    , function(t, e, n) {
        function r(t) {
            var e = !1
              , n = !1
              , r = !1 !== t.jsonp;
            if ("undefined" != typeof location) {
                var a = "https:" === location.protocol
                  , c = location.port;
                c || (c = a ? 443 : 80),
                e = t.hostname !== location.hostname || c !== t.port,
                n = t.secure !== a
            }
            if (t.xdomain = e,
            t.xscheme = n,
            "open"in new o(t) && !t.forceJSONP)
                return new i(t);
            if (!r)
                throw new Error("JSONP disabled");
            return new s(t)
        }
        var o = n(10)
          , i = n(37)
          , s = n(36)
          , a = n(38);
        e.polling = r,
        e.websocket = a
    }
    , function(t, e, n) {
        function r(t) {
            var e = t && t.forceBase64;
            f && !e || (this.supportsBinary = !1),
            o.call(this, t)
        }
        var o = n(9)
          , i = n(5)
          , s = n(1)
          , a = n(3)
          , c = n(23)
          , u = n(4)("engine.io-client:polling");
        t.exports = r;
        var f = function() {
            return null != new (n(10))({
                xdomain: !1
            }).responseType
        }();
        a(r, o),
        r.prototype.name = "polling",
        r.prototype.doOpen = function() {
            this.poll()
        }
        ,
        r.prototype.pause = function(t) {
            function e() {
                u("paused"),
                n.readyState = "paused",
                t()
            }
            var n = this;
            if (this.readyState = "pausing",
            this.polling || !this.writable) {
                var r = 0;
                this.polling && (u("we are currently polling - waiting to pause"),
                r++,
                this.once("pollComplete", function() {
                    u("pre-pause polling complete"),
                    --r || e()
                })),
                this.writable || (u("we are currently writing - waiting to pause"),
                r++,
                this.once("drain", function() {
                    u("pre-pause writing complete"),
                    --r || e()
                }))
            } else
                e()
        }
        ,
        r.prototype.poll = function() {
            u("polling"),
            this.polling = !0,
            this.doPoll(),
            this.emit("poll")
        }
        ,
        r.prototype.onData = function(t) {
            var e = this;
            u("polling got data %s", t);
            var n = function(t, n, r) {
                if ("opening" === e.readyState && "open" === t.type && e.onOpen(),
                "close" === t.type)
                    return e.onClose(),
                    !1;
                e.onPacket(t)
            };
            s.decodePayload(t, this.socket.binaryType, n),
            "closed" !== this.readyState && (this.polling = !1,
            this.emit("pollComplete"),
            "open" === this.readyState ? this.poll() : u('ignoring poll - transport state "%s"', this.readyState))
        }
        ,
        r.prototype.doClose = function() {
            function t() {
                u("writing close packet"),
                e.write([{
                    type: "close"
                }])
            }
            var e = this;
            "open" === this.readyState ? (u("transport open - closing"),
            t()) : (u("transport not open - deferring close"),
            this.once("open", t))
        }
        ,
        r.prototype.write = function(t) {
            var e = this;
            this.writable = !1;
            var n = function() {
                e.writable = !0,
                e.emit("drain")
            };
            s.encodePayload(t, this.supportsBinary, function(t) {
                e.doWrite(t, n)
            })
        }
        ,
        r.prototype.uri = function() {
            var t = this.query || {}
              , e = this.secure ? "https" : "http"
              , n = "";
            return !1 !== this.timestampRequests && (t[this.timestampParam] = c()),
            this.supportsBinary || t.sid || (t.b64 = 1),
            t = i.encode(t),
            this.port && ("https" === e && 443 !== Number(this.port) || "http" === e && 80 !== Number(this.port)) && (n = ":" + this.port),
            t.length && (t = "?" + t),
            e + "://" + (-1 !== this.hostname.indexOf(":") ? "[" + this.hostname + "]" : this.hostname) + n + this.path + t
        }
    }
    , function(t, e, n) {
        (function(e) {
            function r(t) {
                if (!t || "object" != typeof t)
                    return !1;
                if (o(t)) {
                    for (var n = 0, i = t.length; n < i; n++)
                        if (r(t[n]))
                            return !0;
                    return !1
                }
                if ("function" == typeof e && e.isBuffer && e.isBuffer(t) || "function" == typeof ArrayBuffer && t instanceof ArrayBuffer || s && t instanceof Blob || a && t instanceof File)
                    return !0;
                if (t.toJSON && "function" == typeof t.toJSON && 1 === arguments.length)
                    return r(t.toJSON(), !0);
                for (var c in t)
                    if (Object.prototype.hasOwnProperty.call(t, c) && r(t[c]))
                        return !0;
                return !1
            }
            var o = n(43)
              , i = Object.prototype.toString
              , s = "function" == typeof Blob || "undefined" != typeof Blob && "[object BlobConstructor]" === i.call(Blob)
              , a = "function" == typeof File || "undefined" != typeof File && "[object FileConstructor]" === i.call(File);
            t.exports = r
        }
        ).call(e, n(7).Buffer)
    }
    , function(t, e) {
        var n = [].indexOf;
        t.exports = function(t, e) {
            if (n)
                return t.indexOf(e);
            for (var r = 0; r < t.length; ++r)
                if (t[r] === e)
                    return r;
            return -1
        }
    }
    , function(t, e) {
        function n(t, e) {
            var n = /\/{2,9}/g
              , r = e.replace(n, "/").split("/");
            return "/" != e.substr(0, 1) && 0 !== e.length || r.splice(0, 1),
            "/" == e.substr(e.length - 1, 1) && r.splice(r.length - 1, 1),
            r
        }
        function r(t, e) {
            var n = {};
            return e.replace(/(?:^|&)([^&=]*)=?([^&]*)/g, function(t, e, r) {
                e && (n[e] = r)
            }),
            n
        }
        var o = /^(?:(?![^:@]+:[^:@\/]*@)(http|https|ws|wss):\/\/)?((?:(([^:@]*)(?::([^:@]*))?)?@)?((?:[a-f0-9]{0,4}:){2,7}[a-f0-9]{0,4}|[^:\/?#]*)(?::(\d*))?)(((\/(?:[^?#](?![^?#\/]*\.[^?#\/.]+(?:[?#]|$)))*\/?)?([^?#\/]*))(?:\?([^#]*))?(?:#(.*))?)/
          , i = ["source", "protocol", "authority", "userInfo", "user", "password", "host", "port", "relative", "path", "directory", "file", "query", "anchor"];
        t.exports = function(t) {
            var e = t
              , s = t.indexOf("[")
              , a = t.indexOf("]");
            -1 != s && -1 != a && (t = t.substring(0, s) + t.substring(s, a).replace(/:/g, ";") + t.substring(a, t.length));
            for (var c = o.exec(t || ""), u = {}, f = 14; f--; )
                u[i[f]] = c[f] || "";
            return -1 != s && -1 != a && (u.source = e,
            u.host = u.host.substring(1, u.host.length - 1).replace(/;/g, ":"),
            u.authority = u.authority.replace("[", "").replace("]", "").replace(/;/g, ":"),
            u.ipv6uri = !0),
            u.pathNames = n(u, u.path),
            u.queryKey = r(u, u.query),
            u
        }
    }
    , function(t, e, n) {
        function r(t, e) {
            if (!(this instanceof r))
                return new r(t,e);
            t && "object" == typeof t && (e = t,
            t = void 0),
            e = e || {},
            e.path = e.path || "/socket.io",
            this.nsps = {},
            this.subs = [],
            this.opts = e,
            this.reconnection(!1 !== e.reconnection),
            this.reconnectionAttempts(e.reconnectionAttempts || 1 / 0),
            this.reconnectionDelay(e.reconnectionDelay || 1e3),
            this.reconnectionDelayMax(e.reconnectionDelayMax || 5e3),
            this.randomizationFactor(e.randomizationFactor || .5),
            this.backoff = new p({
                min: this.reconnectionDelay(),
                max: this.reconnectionDelayMax(),
                jitter: this.randomizationFactor()
            }),
            this.timeout(null == e.timeout ? 2e4 : e.timeout),
            this.readyState = "closed",
            this.uri = t,
            this.connecting = [],
            this.lastPing = null,
            this.encoding = !1,
            this.packetBuffer = [];
            var n = e.parser || a;
            this.encoder = new n.Encoder,
            this.decoder = new n.Decoder,
            this.autoConnect = !1 !== e.autoConnect,
            this.autoConnect && this.open()
        }
        var o = n(34)
          , i = n(20)
          , s = n(0)
          , a = n(11)
          , c = n(19)
          , u = n(12)
          , f = n(2)("socket.io-client:manager")
          , h = n(16)
          , p = n(30)
          , l = Object.prototype.hasOwnProperty;
        t.exports = r,
        r.prototype.emitAll = function() {
            this.emit.apply(this, arguments);
            for (var t in this.nsps)
                l.call(this.nsps, t) && this.nsps[t].emit.apply(this.nsps[t], arguments)
        }
        ,
        r.prototype.updateSocketIds = function() {
            for (var t in this.nsps)
                l.call(this.nsps, t) && (this.nsps[t].id = this.generateId(t))
        }
        ,
        r.prototype.generateId = function(t) {
            return ("/" === t ? "" : t + "#") + this.engine.id
        }
        ,
        s(r.prototype),
        r.prototype.reconnection = function(t) {
            return arguments.length ? (this._reconnection = !!t,
            this) : this._reconnection
        }
        ,
        r.prototype.reconnectionAttempts = function(t) {
            return arguments.length ? (this._reconnectionAttempts = t,
            this) : this._reconnectionAttempts
        }
        ,
        r.prototype.reconnectionDelay = function(t) {
            return arguments.length ? (this._reconnectionDelay = t,
            this.backoff && this.backoff.setMin(t),
            this) : this._reconnectionDelay
        }
        ,
        r.prototype.randomizationFactor = function(t) {
            return arguments.length ? (this._randomizationFactor = t,
            this.backoff && this.backoff.setJitter(t),
            this) : this._randomizationFactor
        }
        ,
        r.prototype.reconnectionDelayMax = function(t) {
            return arguments.length ? (this._reconnectionDelayMax = t,
            this.backoff && this.backoff.setMax(t),
            this) : this._reconnectionDelayMax
        }
        ,
        r.prototype.timeout = function(t) {
            return arguments.length ? (this._timeout = t,
            this) : this._timeout
        }
        ,
        r.prototype.maybeReconnectOnOpen = function() {
            !this.reconnecting && this._reconnection && 0 === this.backoff.attempts && this.reconnect()
        }
        ,
        r.prototype.open = r.prototype.connect = function(t, e) {
            if (f("readyState %s", this.readyState),
            ~this.readyState.indexOf("open"))
                return this;
            f("opening %s", this.uri),
            this.engine = o(this.uri, this.opts);
            var n = this.engine
              , r = this;
            this.readyState = "opening",
            this.skipReconnect = !1;
            var i = c(n, "open", function() {
                r.onopen(),
                t && t()
            })
              , s = c(n, "error", function(e) {
                if (f("connect_error"),
                r.cleanup(),
                r.readyState = "closed",
                r.emitAll("connect_error", e),
                t) {
                    var n = new Error("Connection error");
                    n.data = e,
                    t(n)
                } else
                    r.maybeReconnectOnOpen()
            });
            if (!1 !== this._timeout) {
                var a = this._timeout;
                f("connect attempt will timeout after %d", a),
                0 === a && i.destroy();
                var u = setTimeout(function() {
                    f("connect attempt timed out after %d", a),
                    i.destroy(),
                    n.close(),
                    n.emit("error", "timeout"),
                    r.emitAll("connect_timeout", a)
                }, a);
                this.subs.push({
                    destroy: function() {
                        clearTimeout(u)
                    }
                })
            }
            return this.subs.push(i),
            this.subs.push(s),
            this
        }
        ,
        r.prototype.onopen = function() {
            f("open"),
            this.cleanup(),
            this.readyState = "open",
            this.emit("open");
            var t = this.engine;
            this.subs.push(c(t, "data", u(this, "ondata"))),
            this.subs.push(c(t, "ping", u(this, "onping"))),
            this.subs.push(c(t, "pong", u(this, "onpong"))),
            this.subs.push(c(t, "error", u(this, "onerror"))),
            this.subs.push(c(t, "close", u(this, "onclose"))),
            this.subs.push(c(this.decoder, "decoded", u(this, "ondecoded")))
        }
        ,
        r.prototype.onping = function() {
            this.lastPing = new Date,
            this.emitAll("ping")
        }
        ,
        r.prototype.onpong = function() {
            this.emitAll("pong", new Date - this.lastPing)
        }
        ,
        r.prototype.ondata = function(t) {
            this.decoder.add(t)
        }
        ,
        r.prototype.ondecoded = function(t) {
            this.emit("packet", t)
        }
        ,
        r.prototype.onerror = function(t) {
            f("error", t),
            this.emitAll("error", t)
        }
        ,
        r.prototype.socket = function(t, e) {
            function n() {
                ~h(o.connecting, r) || o.connecting.push(r)
            }
            var r = this.nsps[t];
            if (!r) {
                r = new i(this,t,e),
                this.nsps[t] = r;
                var o = this;
                r.on("connecting", n),
                r.on("connect", function() {
                    r.id = o.generateId(t)
                }),
                this.autoConnect && n()
            }
            return r
        }
        ,
        r.prototype.destroy = function(t) {
            var e = h(this.connecting, t);
            ~e && this.connecting.splice(e, 1),
            this.connecting.length || this.close()
        }
        ,
        r.prototype.packet = function(t) {
            f("writing packet %j", t);
            var e = this;
            t.query && 0 === t.type && (t.nsp += "?" + t.query),
            e.encoding ? e.packetBuffer.push(t) : (e.encoding = !0,
            this.encoder.encode(t, function(n) {
                for (var r = 0; r < n.length; r++)
                    e.engine.write(n[r], t.options);
                e.encoding = !1,
                e.processPacketQueue()
            }))
        }
        ,
        r.prototype.processPacketQueue = function() {
            if (this.packetBuffer.length > 0 && !this.encoding) {
                var t = this.packetBuffer.shift();
                this.packet(t)
            }
        }
        ,
        r.prototype.cleanup = function() {
            f("cleanup");
            for (var t = this.subs.length, e = 0; e < t; e++) {
                this.subs.shift().destroy()
            }
            this.packetBuffer = [],
            this.encoding = !1,
            this.lastPing = null,
            this.decoder.destroy()
        }
        ,
        r.prototype.close = r.prototype.disconnect = function() {
            f("disconnect"),
            this.skipReconnect = !0,
            this.reconnecting = !1,
            "opening" === this.readyState && this.cleanup(),
            this.backoff.reset(),
            this.readyState = "closed",
            this.engine && this.engine.close()
        }
        ,
        r.prototype.onclose = function(t) {
            f("onclose"),
            this.cleanup(),
            this.backoff.reset(),
            this.readyState = "closed",
            this.emit("close", t),
            this._reconnection && !this.skipReconnect && this.reconnect()
        }
        ,
        r.prototype.reconnect = function() {
            if (this.reconnecting || this.skipReconnect)
                return this;
            var t = this;
            if (this.backoff.attempts >= this._reconnectionAttempts)
                f("reconnect failed"),
                this.backoff.reset(),
                this.emitAll("reconnect_failed"),
                this.reconnecting = !1;
            else {
                var e = this.backoff.duration();
                f("will wait %dms before reconnect attempt", e),
                this.reconnecting = !0;
                var n = setTimeout(function() {
                    t.skipReconnect || (f("attempting reconnect"),
                    t.emitAll("reconnect_attempt", t.backoff.attempts),
                    t.emitAll("reconnecting", t.backoff.attempts),
                    t.skipReconnect || t.open(function(e) {
                        e ? (f("reconnect attempt error"),
                        t.reconnecting = !1,
                        t.reconnect(),
                        t.emitAll("reconnect_error", e.data)) : (f("reconnect success"),
                        t.onreconnect())
                    }))
                }, e);
                this.subs.push({
                    destroy: function() {
                        clearTimeout(n)
                    }
                })
            }
        }
        ,
        r.prototype.onreconnect = function() {
            var t = this.backoff.attempts;
            this.reconnecting = !1,
            this.backoff.reset(),
            this.updateSocketIds(),
            this.emitAll("reconnect", t)
        }
    }
    , function(t, e) {
        function n(t, e, n) {
            return t.on(e, n),
            {
                destroy: function() {
                    t.removeListener(e, n)
                }
            }
        }
        t.exports = n
    }
    , function(t, e, n) {
        function r(t, e, n) {
            this.io = t,
            this.nsp = e,
            this.json = this,
            this.ids = 0,
            this.acks = {},
            this.receiveBuffer = [],
            this.sendBuffer = [],
            this.connected = !1,
            this.disconnected = !0,
            this.flags = {},
            n && n.query && (this.query = n.query),
            this.io.autoConnect && this.open()
        }
        var o = n(11)
          , i = n(0)
          , s = n(51)
          , a = n(19)
          , c = n(12)
          , u = n(2)("socket.io-client:socket")
          , f = n(5)
          , h = n(15);
        t.exports = r;
        var p = {
            connect: 1,
            connect_error: 1,
            connect_timeout: 1,
            connecting: 1,
            disconnect: 1,
            error: 1,
            reconnect: 1,
            reconnect_attempt: 1,
            reconnect_failed: 1,
            reconnect_error: 1,
            reconnecting: 1,
            ping: 1,
            pong: 1
        }
          , l = i.prototype.emit;
        i(r.prototype),
        r.prototype.subEvents = function() {
            if (!this.subs) {
                var t = this.io;
                this.subs = [a(t, "open", c(this, "onopen")), a(t, "packet", c(this, "onpacket")), a(t, "close", c(this, "onclose"))]
            }
        }
        ,
        r.prototype.open = r.prototype.connect = function() {
            return this.connected ? this : (this.subEvents(),
            this.io.reconnecting || this.io.open(),
            "open" === this.io.readyState && this.onopen(),
            this.emit("connecting"),
            this)
        }
        ,
        r.prototype.send = function() {
            var t = s(arguments);
            return t.unshift("message"),
            this.emit.apply(this, t),
            this
        }
        ,
        r.prototype.emit = function(t) {
            if (p.hasOwnProperty(t))
                return l.apply(this, arguments),
                this;
            var e = s(arguments)
              , n = {
                type: (void 0 !== this.flags.binary ? this.flags.binary : h(e)) ? o.BINARY_EVENT : o.EVENT,
                data: e
            };
            return n.options = {},
            n.options.compress = !this.flags || !1 !== this.flags.compress,
            "function" == typeof e[e.length - 1] && (u("emitting packet with ack id %d", this.ids),
            this.acks[this.ids] = e.pop(),
            n.id = this.ids++),
            this.connected ? this.packet(n) : this.sendBuffer.push(n),
            this.flags = {},
            this
        }
        ,
        r.prototype.packet = function(t) {
            t.nsp = this.nsp,
            this.io.packet(t)
        }
        ,
        r.prototype.onopen = function() {
            if (u("transport is open - connecting"),
            "/" !== this.nsp)
                if (this.query) {
                    var t = "object" == typeof this.query ? f.encode(this.query) : this.query;
                    u("sending connect packet with query %s", t),
                    this.packet({
                        type: o.CONNECT,
                        query: t
                    })
                } else
                    this.packet({
                        type: o.CONNECT
                    })
        }
        ,
        r.prototype.onclose = function(t) {
            u("close (%s)", t),
            this.connected = !1,
            this.disconnected = !0,
            delete this.id,
            this.emit("disconnect", t)
        }
        ,
        r.prototype.onpacket = function(t) {
            var e = t.nsp === this.nsp
              , n = t.type === o.ERROR && "/" === t.nsp;
            if (e || n)
                switch (t.type) {
                case o.CONNECT:
                    this.onconnect();
                    break;
                case o.EVENT:
                case o.BINARY_EVENT:
                    this.onevent(t);
                    break;
                case o.ACK:
                case o.BINARY_ACK:
                    this.onack(t);
                    break;
                case o.DISCONNECT:
                    this.ondisconnect();
                    break;
                case o.ERROR:
                    this.emit("error", t.data)
                }
        }
        ,
        r.prototype.onevent = function(t) {
            var e = t.data || [];
            u("emitting event %j", e),
            null != t.id && (u("attaching ack callback to event"),
            e.push(this.ack(t.id))),
            this.connected ? l.apply(this, e) : this.receiveBuffer.push(e)
        }
        ,
        r.prototype.ack = function(t) {
            var e = this
              , n = !1;
            return function() {
                if (!n) {
                    n = !0;
                    var r = s(arguments);
                    u("sending ack %j", r),
                    e.packet({
                        type: h(r) ? o.BINARY_ACK : o.ACK,
                        id: t,
                        data: r
                    })
                }
            }
        }
        ,
        r.prototype.onack = function(t) {
            var e = this.acks[t.id];
            "function" == typeof e ? (u("calling ack %s with %j", t.id, t.data),
            e.apply(this, t.data),
            delete this.acks[t.id]) : u("bad ack %s", t.id)
        }
        ,
        r.prototype.onconnect = function() {
            this.connected = !0,
            this.disconnected = !1,
            this.emitBuffered(),
            this.emit("connect")
        }
        ,
        r.prototype.emitBuffered = function() {
            var t;
            for (t = 0; t < this.receiveBuffer.length; t++)
                l.apply(this, this.receiveBuffer[t]);
            for (this.receiveBuffer = [],
            t = 0; t < this.sendBuffer.length; t++)
                this.packet(this.sendBuffer[t]);
            this.sendBuffer = []
        }
        ,
        r.prototype.ondisconnect = function() {
            u("server disconnect (%s)", this.nsp),
            this.destroy(),
            this.onclose("io server disconnect")
        }
        ,
        r.prototype.destroy = function() {
            if (this.subs) {
                for (var t = 0; t < this.subs.length; t++)
                    this.subs[t].destroy();
                this.subs = null
            }
            this.io.destroy(this)
        }
        ,
        r.prototype.close = r.prototype.disconnect = function() {
            return this.connected && (u("performing disconnect (%s)", this.nsp),
            this.packet({
                type: o.DISCONNECT
            })),
            this.destroy(),
            this.connected && this.onclose("io client disconnect"),
            this
        }
        ,
        r.prototype.compress = function(t) {
            return this.flags.compress = t,
            this
        }
        ,
        r.prototype.binary = function(t) {
            return this.flags.binary = t,
            this
        }
    }
    , function(t, e) {
        var n = {}.toString;
        t.exports = Array.isArray || function(t) {
            return "[object Array]" == n.call(t)
        }
    }
    , function(t, e, n) {
        (function(e) {
            function n(t) {
                return r && e.isBuffer(t) || o && (t instanceof ArrayBuffer || i(t))
            }
            t.exports = n;
            var r = "function" == typeof e && "function" == typeof e.isBuffer
              , o = "function" == typeof ArrayBuffer
              , i = function(t) {
                return "function" == typeof ArrayBuffer.isView ? ArrayBuffer.isView(t) : t.buffer instanceof ArrayBuffer
            }
        }
        ).call(e, n(7).Buffer)
    }
    , function(t, e, n) {
        "use strict";
        function r(t) {
            var e = "";
            do {
                e = a[t % c] + e,
                t = Math.floor(t / c)
            } while (t > 0);
            return e
        }
        function o(t) {
            var e = 0;
            for (h = 0; h < t.length; h++)
                e = e * c + u[t.charAt(h)];
            return e
        }
        function i() {
            var t = r(+new Date);
            return t !== s ? (f = 0,
            s = t) : t + "." + r(f++)
        }
        for (var s, a = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_".split(""), c = 64, u = {}, f = 0, h = 0; h < c; h++)
            u[a[h]] = h;
        i.encode = r,
        i.decode = o,
        t.exports = i
    }
    , function(t, e, n) {
        "use strict";
        var r = function(t, e) {
            if (!window.UCFooPlugin)
                return void console.error("can not get UCFooPlugin");
            window.UCFooPlugin.toast(t, e)
        }
          , o = function(t, e) {
            if (r && window.UCFooPlugin && window.UCFooPlugin.toast)
                r(t, e);
            else {
                var n = /Mobile/.test(navigator.userAgent);
                e = isNaN(e) ? 3e3 : e;
                var o = document.createElement("div")
                  , i = document.createElement("div");
                i.style.cssText = "position: absolute;opacity0;",
                o.innerHTML = t;
                o.style.cssText = n ? "padding:0px 0.187rem;height: 1.28rem;color: rgb(255, 255, 255);line-height: 1.28rem;text-align: center;border-radius: 0.053rem;position: fixed;top: 50%;left: 50%;transform: translate(-50%, -50%);z-index: 999999;background: rgba(0, 0, 0,.7);font-size: 0.4rem;" : "padding:0 14px;height: 48px;color: rgb(255, 255, 255);line-height: 48px;text-align: center;border-radius: 4px;position: fixed;top: 50%;left: 50%;transform: translate(-50%, -50%);z-index: 999999;background: rgba(0, 0, 0,.7);font-size: 14px;",
                i.appendChild(o),
                document.body.appendChild(i);
                var s = o.offsetWidth;
                o.style.width = s + "px",
                o.style.maxWidth = n ? "80%" : "40%",
                o.style.minWidth = n ? "50%" : "100px",
                i.style.opacity = "1";
                var a = setTimeout(function() {
                    var t = .5;
                    o.style.webkitTransition = "-webkit-transform " + t + "s ease-in, opacity " + t + "s ease-in",
                    o.style.opacity = "0";
                    var e = setTimeout(function() {
                        document.body.removeChild(i),
                        clearTimeout(a),
                        clearTimeout(e)
                    }, 1e3 * t)
                }, e)
            }
        };
        t.exports = function() {
            var t = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : "网络不给力，请检查网络。"
              , e = arguments[1];
            o(t, e)
        }
    }
    , function(t, e, n) {
        "use strict";
        function r(t) {
            function e() {
                n(),
                document.addEventListener("click", o, !1),
                document.addEventListener("touchend", o, !1),
                document.addEventListener("contextmenu", o, !1),
                window.addEventListener("resize", r, !1)
            }
            function n() {
                c = document.createElement("div"),
                c.style.width = window.innerWidth + "px",
                c.style.height = window.innerHeight + "px",
                c.style.position = "fixed",
                c.style.left = 0,
                c.style.top = 0,
                c.style.zIndex = 999999999,
                c.style.backgroundColor = "rgba(0,0,0,0.7)",
                c.style.display = "block";
                var e = '\n            <div style="width: 500px; height: 300px; background: #fff; position: absolute; left: 50%; top: 50%; transform: translate(-50%, -50%); font-size: 16px; border-radius: 2px; overflow: hidden;">\n                <p style="width: 100%; height: 40px; line-height: 40px; font-weight: 700; background: #efefef; color: #333; text-indent: 28px; border-bottom: 1px solid #ccd0d6; margin: 0;">提示</p>\n                <div style="text-align: center; padding-top: 55px;">\n                    <p style="width: 100%; text-align: center; font-weight: 700; margin: 0;">' + t + "</p>\n                    <button id=_mask_notice_id_" + u + ' style="width: 120px; height: 34px; line-height: 34px; background: rgb(41, 155, 228); color: #fff; margin: 85px 0 0; border: none; outline: none; box-shadow: rgba(0, 0, 0, 0.12) 0px 1px 6px, rgba(0, 0, 0, 0.12) 0px 1px 4px; border-radius: 2px; cursor: pointer; border-radius: 4px;">确定</button>\n                </div>\n            </div>\n        '
                  , n = '\n            <div style="width: 75%; height: 4rem; background: #fff; position: absolute; left: 50%; top: 50%; transform: translate(-50%, -50%); font-size: 0.32rem;">\n                <p style="width: 100%; height: 0.8rem; line-height: 0.8rem; font-size: 0.32rem; font-weight: 400; background: #efefef; color: #333; text-indent: 0.3rem; border-bottom: 1px solid #ccd0d6; margin: 0;">提示</p>\n                <div style="text-align: center; padding-top: 0.5rem;">\n                    <p style="text-align: center; font-weight: 400; margin: 0; font-size: 0.32rem;">' + t + "</p>\n                    <button id=_mask_notice_id_" + u + ' style="width: 1.8rem; height: 0.8rem; line-height: 0.8rem; font-size: 0.32rem; background: rgb(41, 155, 228); color: #fff; margin: 0.8rem 0 0; border: none; outline: none; box-shadow: rgba(0, 0, 0, 0.12) 0px 1px 6px, rgba(0, 0, 0, 0.12) 0px 1px 4px; border-radius: 2px; cursor: pointer;">确定</button>\n                </div>\n            </div>\n        '
                  , r = /Mobile/.test(navigator.userAgent);
                c.innerHTML = r ? n : e,
                document.body.appendChild(c)
            }
            function r() {
                c.style.width = window.innerWidth + "px",
                c.style.height = window.innerHeight + "px"
            }
            function o(t) {
                t.stopPropagation(),
                "contextmenu" === t.type && (t.returnValue = !1),
                "click" !== t.type && "touchend" !== t.type || t.target.id !== "_mask_notice_id_" + u || (i(),
                s())
            }
            function i() {
                a && a()
            }
            function s() {
                document.removeEventListener("click", o, !1),
                document.removeEventListener("touchend", o, !1),
                document.removeEventListener("contextmenu", o, !1),
                window.removeEventListener("resize", r, !1),
                document.body.removeChild(c)
            }
            var a, c = null, t = t || "由于你长时间未操作，请点击确定继续学习。", u = (new Date).getTime();
            return e(),
            {
                onClose: function(t) {
                    a = t
                },
                close: function() {
                    s()
                }
            }
        }
        t.exports = function() {
            var t = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : "由于你长时间未操作，请点确定继续使用。"
              , e = arguments[1];
            r(t).onClose(e)
        }
    }
    , function(t, e, n) {
        function r(t, e) {
            "object" == typeof t && (e = t,
            t = void 0),
            e = e || {};
            var n, r = o(t), i = r.source, u = r.id, f = r.path, h = c[u] && f in c[u].nsps, p = e.forceNew || e["force new connection"] || !1 === e.multiplex || h;
            return p ? (a("ignoring socket cache for %s", i),
            n = s(i, e)) : (c[u] || (a("new io instance for %s", i),
            c[u] = s(i, e)),
            n = c[u]),
            r.query && !e.query && (e.query = r.query),
            n.socket(r.path, e)
        }
        var o = n(47)
          , i = n(11)
          , s = n(18)
          , a = n(2)("socket.io-client");
        t.exports = e = r;
        var c = e.managers = {};
        e.protocol = i.protocol,
        e.connect = r,
        e.Manager = n(18),
        e.Socket = n(20)
    }
    , function(t, e, n) {
        var r;
        !function(e) {
            "use strict";
            function o() {}
            function i(t, e) {
                for (var n = t.length; n--; )
                    if (t[n].listener === e)
                        return n;
                return -1
            }
            function s(t) {
                return function() {
                    return this[t].apply(this, arguments)
                }
            }
            function a(t) {
                return "function" == typeof t || t instanceof RegExp || !(!t || "object" != typeof t) && a(t.listener)
            }
            var c = o.prototype
              , u = e.EventEmitter;
            c.getListeners = function(t) {
                var e, n, r = this._getEvents();
                if (t instanceof RegExp) {
                    e = {};
                    for (n in r)
                        r.hasOwnProperty(n) && t.test(n) && (e[n] = r[n])
                } else
                    e = r[t] || (r[t] = []);
                return e
            }
            ,
            c.flattenListeners = function(t) {
                var e, n = [];
                for (e = 0; e < t.length; e += 1)
                    n.push(t[e].listener);
                return n
            }
            ,
            c.getListenersAsObject = function(t) {
                var e, n = this.getListeners(t);
                return n instanceof Array && (e = {},
                e[t] = n),
                e || n
            }
            ,
            c.addListener = function(t, e) {
                if (!a(e))
                    throw new TypeError("listener must be a function");
                var n, r = this.getListenersAsObject(t), o = "object" == typeof e;
                for (n in r)
                    r.hasOwnProperty(n) && -1 === i(r[n], e) && r[n].push(o ? e : {
                        listener: e,
                        once: !1
                    });
                return this
            }
            ,
            c.on = s("addListener"),
            c.addOnceListener = function(t, e) {
                return this.addListener(t, {
                    listener: e,
                    once: !0
                })
            }
            ,
            c.once = s("addOnceListener"),
            c.defineEvent = function(t) {
                return this.getListeners(t),
                this
            }
            ,
            c.defineEvents = function(t) {
                for (var e = 0; e < t.length; e += 1)
                    this.defineEvent(t[e]);
                return this
            }
            ,
            c.removeListener = function(t, e) {
                var n, r, o = this.getListenersAsObject(t);
                for (r in o)
                    o.hasOwnProperty(r) && -1 !== (n = i(o[r], e)) && o[r].splice(n, 1);
                return this
            }
            ,
            c.off = s("removeListener"),
            c.addListeners = function(t, e) {
                return this.manipulateListeners(!1, t, e)
            }
            ,
            c.removeListeners = function(t, e) {
                return this.manipulateListeners(!0, t, e)
            }
            ,
            c.manipulateListeners = function(t, e, n) {
                var r, o, i = t ? this.removeListener : this.addListener, s = t ? this.removeListeners : this.addListeners;
                if ("object" != typeof e || e instanceof RegExp)
                    for (r = n.length; r--; )
                        i.call(this, e, n[r]);
                else
                    for (r in e)
                        e.hasOwnProperty(r) && (o = e[r]) && ("function" == typeof o ? i.call(this, r, o) : s.call(this, r, o));
                return this
            }
            ,
            c.removeEvent = function(t) {
                var e, n = typeof t, r = this._getEvents();
                if ("string" === n)
                    delete r[t];
                else if (t instanceof RegExp)
                    for (e in r)
                        r.hasOwnProperty(e) && t.test(e) && delete r[e];
                else
                    delete this._events;
                return this
            }
            ,
            c.removeAllListeners = s("removeEvent"),
            c.emitEvent = function(t, e) {
                var n, r, o, i, s = this.getListenersAsObject(t);
                for (i in s)
                    if (s.hasOwnProperty(i))
                        for (n = s[i].slice(0),
                        o = 0; o < n.length; o++)
                            r = n[o],
                            !0 === r.once && this.removeListener(t, r.listener),
                            r.listener.apply(this, e || []) === this._getOnceReturnValue() && this.removeListener(t, r.listener);
                return this
            }
            ,
            c.trigger = s("emitEvent"),
            c.emit = function(t) {
                var e = Array.prototype.slice.call(arguments, 1);
                return this.emitEvent(t, e)
            }
            ,
            c.setOnceReturnValue = function(t) {
                return this._onceReturnValue = t,
                this
            }
            ,
            c._getOnceReturnValue = function() {
                return !this.hasOwnProperty("_onceReturnValue") || this._onceReturnValue
            }
            ,
            c._getEvents = function() {
                return this._events || (this._events = {})
            }
            ,
            o.noConflict = function() {
                return e.EventEmitter = u,
                o
            }
            ,
            void 0 !== (r = function() {
                return o
            }
            .call(e, n, e, t)) && (t.exports = r)
        }("undefined" != typeof window ? window : this || {})
    }
    , function(t, e) {
        function n(t, e, n) {
            function o(t, r) {
                if (o.count <= 0)
                    throw new Error("after called too many times");
                --o.count,
                t ? (i = !0,
                e(t),
                e = n) : 0 !== o.count || i || e(null, r)
            }
            var i = !1;
            return n = n || r,
            o.count = t,
            0 === t ? e() : o
        }
        function r() {}
        t.exports = n
    }
    , function(t, e) {
        t.exports = function(t, e, n) {
            var r = t.byteLength;
            if (e = e || 0,
            n = n || r,
            t.slice)
                return t.slice(e, n);
            if (e < 0 && (e += r),
            n < 0 && (n += r),
            n > r && (n = r),
            e >= r || e >= n || 0 === r)
                return new ArrayBuffer(0);
            for (var o = new Uint8Array(t), i = new Uint8Array(n - e), s = e, a = 0; s < n; s++,
            a++)
                i[a] = o[s];
            return i.buffer
        }
    }
    , function(t, e) {
        function n(t) {
            t = t || {},
            this.ms = t.min || 100,
            this.max = t.max || 1e4,
            this.factor = t.factor || 2,
            this.jitter = t.jitter > 0 && t.jitter <= 1 ? t.jitter : 0,
            this.attempts = 0
        }
        t.exports = n,
        n.prototype.duration = function() {
            var t = this.ms * Math.pow(this.factor, this.attempts++);
            if (this.jitter) {
                var e = Math.random()
                  , n = Math.floor(e * this.jitter * t);
                t = 0 == (1 & Math.floor(10 * e)) ? t - n : t + n
            }
            return 0 | Math.min(t, this.max)
        }
        ,
        n.prototype.reset = function() {
            this.attempts = 0
        }
        ,
        n.prototype.setMin = function(t) {
            this.ms = t
        }
        ,
        n.prototype.setMax = function(t) {
            this.max = t
        }
        ,
        n.prototype.setJitter = function(t) {
            this.jitter = t
        }
    }
    , function(t, e) {
        !function(t) {
            "use strict";
            e.encode = function(e) {
                var n, r = new Uint8Array(e), o = r.length, i = "";
                for (n = 0; n < o; n += 3)
                    i += t[r[n] >> 2],
                    i += t[(3 & r[n]) << 4 | r[n + 1] >> 4],
                    i += t[(15 & r[n + 1]) << 2 | r[n + 2] >> 6],
                    i += t[63 & r[n + 2]];
                return o % 3 == 2 ? i = i.substring(0, i.length - 1) + "=" : o % 3 == 1 && (i = i.substring(0, i.length - 2) + "=="),
                i
            }
            ,
            e.decode = function(e) {
                var n, r, o, i, s, a = .75 * e.length, c = e.length, u = 0;
                "=" === e[e.length - 1] && (a--,
                "=" === e[e.length - 2] && a--);
                var f = new ArrayBuffer(a)
                  , h = new Uint8Array(f);
                for (n = 0; n < c; n += 4)
                    r = t.indexOf(e[n]),
                    o = t.indexOf(e[n + 1]),
                    i = t.indexOf(e[n + 2]),
                    s = t.indexOf(e[n + 3]),
                    h[u++] = r << 2 | o >> 4,
                    h[u++] = (15 & o) << 4 | i >> 2,
                    h[u++] = (3 & i) << 6 | 63 & s;
                return f
            }
        }("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/")
    }
    , function(t, e, n) {
        "use strict";
        function r(t) {
            var e = t.length;
            if (e % 4 > 0)
                throw new Error("Invalid string. Length must be a multiple of 4");
            var n = t.indexOf("=");
            return -1 === n && (n = e),
            [n, n === e ? 0 : 4 - n % 4]
        }
        function o(t) {
            var e = r(t)
              , n = e[0]
              , o = e[1];
            return 3 * (n + o) / 4 - o
        }
        function i(t, e, n) {
            return 3 * (e + n) / 4 - n
        }
        function s(t) {
            var e, n, o = r(t), s = o[0], a = o[1], c = new p(i(t, s, a)), u = 0, f = a > 0 ? s - 4 : s;
            for (n = 0; n < f; n += 4)
                e = h[t.charCodeAt(n)] << 18 | h[t.charCodeAt(n + 1)] << 12 | h[t.charCodeAt(n + 2)] << 6 | h[t.charCodeAt(n + 3)],
                c[u++] = e >> 16 & 255,
                c[u++] = e >> 8 & 255,
                c[u++] = 255 & e;
            return 2 === a && (e = h[t.charCodeAt(n)] << 2 | h[t.charCodeAt(n + 1)] >> 4,
            c[u++] = 255 & e),
            1 === a && (e = h[t.charCodeAt(n)] << 10 | h[t.charCodeAt(n + 1)] << 4 | h[t.charCodeAt(n + 2)] >> 2,
            c[u++] = e >> 8 & 255,
            c[u++] = 255 & e),
            c
        }
        function a(t) {
            return f[t >> 18 & 63] + f[t >> 12 & 63] + f[t >> 6 & 63] + f[63 & t]
        }
        function c(t, e, n) {
            for (var r, o = [], i = e; i < n; i += 3)
                r = (t[i] << 16 & 16711680) + (t[i + 1] << 8 & 65280) + (255 & t[i + 2]),
                o.push(a(r));
            return o.join("")
        }
        function u(t) {
            for (var e, n = t.length, r = n % 3, o = [], i = 16383, s = 0, a = n - r; s < a; s += i)
                o.push(c(t, s, s + i > a ? a : s + i));
            return 1 === r ? (e = t[n - 1],
            o.push(f[e >> 2] + f[e << 4 & 63] + "==")) : 2 === r && (e = (t[n - 2] << 8) + t[n - 1],
            o.push(f[e >> 10] + f[e >> 4 & 63] + f[e << 2 & 63] + "=")),
            o.join("")
        }
        e.byteLength = o,
        e.toByteArray = s,
        e.fromByteArray = u;
        for (var f = [], h = [], p = "undefined" != typeof Uint8Array ? Uint8Array : Array, l = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", d = 0, y = l.length; d < y; ++d)
            f[d] = l[d],
            h[l.charCodeAt(d)] = d;
        h["-".charCodeAt(0)] = 62,
        h["_".charCodeAt(0)] = 63
    }
    , function(t, e) {
        function n(t) {
            return t.map(function(t) {
                if (t.buffer instanceof ArrayBuffer) {
                    var e = t.buffer;
                    if (t.byteLength !== e.byteLength) {
                        var n = new Uint8Array(t.byteLength);
                        n.set(new Uint8Array(e,t.byteOffset,t.byteLength)),
                        e = n.buffer
                    }
                    return e
                }
                return t
            })
        }
        function r(t, e) {
            e = e || {};
            var r = new i;
            return n(t).forEach(function(t) {
                r.append(t)
            }),
            e.type ? r.getBlob(e.type) : r.getBlob()
        }
        function o(t, e) {
            return new Blob(n(t),e || {})
        }
        var i = void 0 !== i ? i : "undefined" != typeof WebKitBlobBuilder ? WebKitBlobBuilder : "undefined" != typeof MSBlobBuilder ? MSBlobBuilder : "undefined" != typeof MozBlobBuilder && MozBlobBuilder
          , s = function() {
            try {
                return 2 === new Blob(["hi"]).size
            } catch (t) {
                return !1
            }
        }()
          , a = s && function() {
            try {
                return 2 === new Blob([new Uint8Array([1, 2])]).size
            } catch (t) {
                return !1
            }
        }()
          , c = i && i.prototype.append && i.prototype.getBlob;
        "undefined" != typeof Blob && (r.prototype = Blob.prototype,
        o.prototype = Blob.prototype),
        t.exports = function() {
            return s ? a ? Blob : o : c ? r : void 0
        }()
    }
    , function(t, e, n) {
        t.exports = n(35),
        t.exports.parser = n(1)
    }
    , function(t, e, n) {
        function r(t, e) {
            if (!(this instanceof r))
                return new r(t,e);
            e = e || {},
            t && "object" == typeof t && (e = t,
            t = null),
            t ? (t = f(t),
            e.hostname = t.host,
            e.secure = "https" === t.protocol || "wss" === t.protocol,
            e.port = t.port,
            t.query && (e.query = t.query)) : e.host && (e.hostname = f(e.host).host),
            this.secure = null != e.secure ? e.secure : "undefined" != typeof location && "https:" === location.protocol,
            e.hostname && !e.port && (e.port = this.secure ? "443" : "80"),
            this.agent = e.agent || !1,
            this.hostname = e.hostname || ("undefined" != typeof location ? location.hostname : "localhost"),
            this.port = e.port || ("undefined" != typeof location && location.port ? location.port : this.secure ? 443 : 80),
            this.query = e.query || {},
            "string" == typeof this.query && (this.query = h.decode(this.query)),
            this.upgrade = !1 !== e.upgrade,
            this.path = (e.path || "/engine.io").replace(/\/$/, "") + "/",
            this.forceJSONP = !!e.forceJSONP,
            this.jsonp = !1 !== e.jsonp,
            this.forceBase64 = !!e.forceBase64,
            this.enablesXDR = !!e.enablesXDR,
            this.withCredentials = !1 !== e.withCredentials,
            this.timestampParam = e.timestampParam || "t",
            this.timestampRequests = e.timestampRequests,
            this.transports = e.transports || ["polling", "websocket"],
            this.transportOptions = e.transportOptions || {},
            this.readyState = "",
            this.writeBuffer = [],
            this.prevBufferLen = 0,
            this.policyPort = e.policyPort || 843,
            this.rememberUpgrade = e.rememberUpgrade || !1,
            this.binaryType = null,
            this.onlyBinaryUpgrades = e.onlyBinaryUpgrades,
            this.perMessageDeflate = !1 !== e.perMessageDeflate && (e.perMessageDeflate || {}),
            !0 === this.perMessageDeflate && (this.perMessageDeflate = {}),
            this.perMessageDeflate && null == this.perMessageDeflate.threshold && (this.perMessageDeflate.threshold = 1024),
            this.pfx = e.pfx || void 0,
            this.key = e.key || void 0,
            this.passphrase = e.passphrase || void 0,
            this.cert = e.cert || void 0,
            this.ca = e.ca || void 0,
            this.ciphers = e.ciphers || void 0,
            this.rejectUnauthorized = void 0 === e.rejectUnauthorized || e.rejectUnauthorized,
            this.forceNode = !!e.forceNode,
            this.isReactNative = "undefined" != typeof navigator && "string" == typeof navigator.product && "reactnative" === navigator.product.toLowerCase(),
            ("undefined" == typeof self || this.isReactNative) && (e.extraHeaders && Object.keys(e.extraHeaders).length > 0 && (this.extraHeaders = e.extraHeaders),
            e.localAddress && (this.localAddress = e.localAddress)),
            this.id = null,
            this.upgrades = null,
            this.pingInterval = null,
            this.pingTimeout = null,
            this.pingIntervalTimer = null,
            this.pingTimeoutTimer = null,
            this.open()
        }
        function o(t) {
            var e = {};
            for (var n in t)
                t.hasOwnProperty(n) && (e[n] = t[n]);
            return e
        }
        var i = n(13)
          , s = n(0)
          , a = n(4)("engine.io-client:socket")
          , c = n(16)
          , u = n(1)
          , f = n(17)
          , h = n(5);
        t.exports = r,
        r.priorWebsocketSuccess = !1,
        s(r.prototype),
        r.protocol = u.protocol,
        r.Socket = r,
        r.Transport = n(9),
        r.transports = n(13),
        r.parser = n(1),
        r.prototype.createTransport = function(t) {
            a('creating transport "%s"', t);
            var e = o(this.query);
            e.EIO = u.protocol,
            e.transport = t;
            var n = this.transportOptions[t] || {};
            return this.id && (e.sid = this.id),
            new i[t]({
                query: e,
                socket: this,
                agent: n.agent || this.agent,
                hostname: n.hostname || this.hostname,
                port: n.port || this.port,
                secure: n.secure || this.secure,
                path: n.path || this.path,
                forceJSONP: n.forceJSONP || this.forceJSONP,
                jsonp: n.jsonp || this.jsonp,
                forceBase64: n.forceBase64 || this.forceBase64,
                enablesXDR: n.enablesXDR || this.enablesXDR,
                withCredentials: n.withCredentials || this.withCredentials,
                timestampRequests: n.timestampRequests || this.timestampRequests,
                timestampParam: n.timestampParam || this.timestampParam,
                policyPort: n.policyPort || this.policyPort,
                pfx: n.pfx || this.pfx,
                key: n.key || this.key,
                passphrase: n.passphrase || this.passphrase,
                cert: n.cert || this.cert,
                ca: n.ca || this.ca,
                ciphers: n.ciphers || this.ciphers,
                rejectUnauthorized: n.rejectUnauthorized || this.rejectUnauthorized,
                perMessageDeflate: n.perMessageDeflate || this.perMessageDeflate,
                extraHeaders: n.extraHeaders || this.extraHeaders,
                forceNode: n.forceNode || this.forceNode,
                localAddress: n.localAddress || this.localAddress,
                requestTimeout: n.requestTimeout || this.requestTimeout,
                protocols: n.protocols || void 0,
                isReactNative: this.isReactNative
            })
        }
        ,
        r.prototype.open = function() {
            var t;
            if (this.rememberUpgrade && r.priorWebsocketSuccess && -1 !== this.transports.indexOf("websocket"))
                t = "websocket";
            else {
                if (0 === this.transports.length) {
                    var e = this;
                    return void setTimeout(function() {
                        e.emit("error", "No transports available")
                    }, 0)
                }
                t = this.transports[0]
            }
            this.readyState = "opening";
            try {
                t = this.createTransport(t)
            } catch (t) {
                return this.transports.shift(),
                void this.open()
            }
            t.open(),
            this.setTransport(t)
        }
        ,
        r.prototype.setTransport = function(t) {
            a("setting transport %s", t.name);
            var e = this;
            this.transport && (a("clearing existing transport %s", this.transport.name),
            this.transport.removeAllListeners()),
            this.transport = t,
            t.on("drain", function() {
                e.onDrain()
            }).on("packet", function(t) {
                e.onPacket(t)
            }).on("error", function(t) {
                e.onError(t)
            }).on("close", function() {
                e.onClose("transport close")
            })
        }
        ,
        r.prototype.probe = function(t) {
            function e() {
                if (p.onlyBinaryUpgrades) {
                    var e = !this.supportsBinary && p.transport.supportsBinary;
                    h = h || e
                }
                h || (a('probe transport "%s" opened', t),
                f.send([{
                    type: "ping",
                    data: "probe"
                }]),
                f.once("packet", function(e) {
                    if (!h)
                        if ("pong" === e.type && "probe" === e.data) {
                            if (a('probe transport "%s" pong', t),
                            p.upgrading = !0,
                            p.emit("upgrading", f),
                            !f)
                                return;
                            r.priorWebsocketSuccess = "websocket" === f.name,
                            a('pausing current transport "%s"', p.transport.name),
                            p.transport.pause(function() {
                                h || "closed" !== p.readyState && (a("changing transport and sending upgrade packet"),
                                u(),
                                p.setTransport(f),
                                f.send([{
                                    type: "upgrade"
                                }]),
                                p.emit("upgrade", f),
                                f = null,
                                p.upgrading = !1,
                                p.flush())
                            })
                        } else {
                            a('probe transport "%s" failed', t);
                            var n = new Error("probe error");
                            n.transport = f.name,
                            p.emit("upgradeError", n)
                        }
                }))
            }
            function n() {
                h || (h = !0,
                u(),
                f.close(),
                f = null)
            }
            function o(e) {
                var r = new Error("probe error: " + e);
                r.transport = f.name,
                n(),
                a('probe transport "%s" failed because of error: %s', t, e),
                p.emit("upgradeError", r)
            }
            function i() {
                o("transport closed")
            }
            function s() {
                o("socket closed")
            }
            function c(t) {
                f && t.name !== f.name && (a('"%s" works - aborting "%s"', t.name, f.name),
                n())
            }
            function u() {
                f.removeListener("open", e),
                f.removeListener("error", o),
                f.removeListener("close", i),
                p.removeListener("close", s),
                p.removeListener("upgrading", c)
            }
            a('probing transport "%s"', t);
            var f = this.createTransport(t, {
                probe: 1
            })
              , h = !1
              , p = this;
            r.priorWebsocketSuccess = !1,
            f.once("open", e),
            f.once("error", o),
            f.once("close", i),
            this.once("close", s),
            this.once("upgrading", c),
            f.open()
        }
        ,
        r.prototype.onOpen = function() {
            if (a("socket open"),
            this.readyState = "open",
            r.priorWebsocketSuccess = "websocket" === this.transport.name,
            this.emit("open"),
            this.flush(),
            "open" === this.readyState && this.upgrade && this.transport.pause) {
                a("starting upgrade probes");
                for (var t = 0, e = this.upgrades.length; t < e; t++)
                    this.probe(this.upgrades[t])
            }
        }
        ,
        r.prototype.onPacket = function(t) {
            if ("opening" === this.readyState || "open" === this.readyState || "closing" === this.readyState)
                switch (a('socket receive: type "%s", data "%s"', t.type, t.data),
                this.emit("packet", t),
                this.emit("heartbeat"),
                t.type) {
                case "open":
                    this.onHandshake(JSON.parse(t.data));
                    break;
                case "pong":
                    this.setPing(),
                    this.emit("pong");
                    break;
                case "error":
                    var e = new Error("server error");
                    e.code = t.data,
                    this.onError(e);
                    break;
                case "message":
                    this.emit("data", t.data),
                    this.emit("message", t.data)
                }
            else
                a('packet received with socket readyState "%s"', this.readyState)
        }
        ,
        r.prototype.onHandshake = function(t) {
            this.emit("handshake", t),
            this.id = t.sid,
            this.transport.query.sid = t.sid,
            this.upgrades = this.filterUpgrades(t.upgrades),
            this.pingInterval = t.pingInterval,
            this.pingTimeout = t.pingTimeout,
            this.onOpen(),
            "closed" !== this.readyState && (this.setPing(),
            this.removeListener("heartbeat", this.onHeartbeat),
            this.on("heartbeat", this.onHeartbeat))
        }
        ,
        r.prototype.onHeartbeat = function(t) {
            clearTimeout(this.pingTimeoutTimer);
            var e = this;
            e.pingTimeoutTimer = setTimeout(function() {
                "closed" !== e.readyState && e.onClose("ping timeout")
            }, t || e.pingInterval + e.pingTimeout)
        }
        ,
        r.prototype.setPing = function() {
            var t = this;
            clearTimeout(t.pingIntervalTimer),
            t.pingIntervalTimer = setTimeout(function() {
                a("writing ping packet - expecting pong within %sms", t.pingTimeout),
                t.ping(),
                t.onHeartbeat(t.pingTimeout)
            }, t.pingInterval)
        }
        ,
        r.prototype.ping = function() {
            var t = this;
            this.sendPacket("ping", function() {
                t.emit("ping")
            })
        }
        ,
        r.prototype.onDrain = function() {
            this.writeBuffer.splice(0, this.prevBufferLen),
            this.prevBufferLen = 0,
            0 === this.writeBuffer.length ? this.emit("drain") : this.flush()
        }
        ,
        r.prototype.flush = function() {
            "closed" !== this.readyState && this.transport.writable && !this.upgrading && this.writeBuffer.length && (a("flushing %d packets in socket", this.writeBuffer.length),
            this.transport.send(this.writeBuffer),
            this.prevBufferLen = this.writeBuffer.length,
            this.emit("flush"))
        }
        ,
        r.prototype.write = r.prototype.send = function(t, e, n) {
            return this.sendPacket("message", t, e, n),
            this
        }
        ,
        r.prototype.sendPacket = function(t, e, n, r) {
            if ("function" == typeof e && (r = e,
            e = void 0),
            "function" == typeof n && (r = n,
            n = null),
            "closing" !== this.readyState && "closed" !== this.readyState) {
                n = n || {},
                n.compress = !1 !== n.compress;
                var o = {
                    type: t,
                    data: e,
                    options: n
                };
                this.emit("packetCreate", o),
                this.writeBuffer.push(o),
                r && this.once("flush", r),
                this.flush()
            }
        }
        ,
        r.prototype.close = function() {
            function t() {
                r.onClose("forced close"),
                a("socket closing - telling transport to close"),
                r.transport.close()
            }
            function e() {
                r.removeListener("upgrade", e),
                r.removeListener("upgradeError", e),
                t()
            }
            function n() {
                r.once("upgrade", e),
                r.once("upgradeError", e)
            }
            if ("opening" === this.readyState || "open" === this.readyState) {
                this.readyState = "closing";
                var r = this;
                this.writeBuffer.length ? this.once("drain", function() {
                    this.upgrading ? n() : t()
                }) : this.upgrading ? n() : t()
            }
            return this
        }
        ,
        r.prototype.onError = function(t) {
            a("socket error %j", t),
            r.priorWebsocketSuccess = !1,
            this.emit("error", t),
            this.onClose("transport error", t)
        }
        ,
        r.prototype.onClose = function(t, e) {
            if ("opening" === this.readyState || "open" === this.readyState || "closing" === this.readyState) {
                a('socket close with reason: "%s"', t);
                var n = this;
                clearTimeout(this.pingIntervalTimer),
                clearTimeout(this.pingTimeoutTimer),
                this.transport.removeAllListeners("close"),
                this.transport.close(),
                this.transport.removeAllListeners(),
                this.readyState = "closed",
                this.id = null,
                this.emit("close", t, e),
                n.writeBuffer = [],
                n.prevBufferLen = 0
            }
        }
        ,
        r.prototype.filterUpgrades = function(t) {
            for (var e = [], n = 0, r = t.length; n < r; n++)
                ~c(this.transports, t[n]) && e.push(t[n]);
            return e
        }
    }
    , function(t, e, n) {
        function r() {}
        function o(t) {
            i.call(this, t),
            this.query = this.query || {},
            c || (c = a.___eio = a.___eio || []),
            this.index = c.length;
            var e = this;
            c.push(function(t) {
                e.onData(t)
            }),
            this.query.j = this.index,
            "function" == typeof addEventListener && addEventListener("beforeunload", function() {
                e.script && (e.script.onerror = r)
            }, !1)
        }
        var i = n(14)
          , s = n(3)
          , a = n(8);
        t.exports = o;
        var c, u = /\n/g, f = /\\n/g;
        s(o, i),
        o.prototype.supportsBinary = !1,
        o.prototype.doClose = function() {
            this.script && (this.script.parentNode.removeChild(this.script),
            this.script = null),
            this.form && (this.form.parentNode.removeChild(this.form),
            this.form = null,
            this.iframe = null),
            i.prototype.doClose.call(this)
        }
        ,
        o.prototype.doPoll = function() {
            var t = this
              , e = document.createElement("script");
            this.script && (this.script.parentNode.removeChild(this.script),
            this.script = null),
            e.async = !0,
            e.src = this.uri(),
            e.onerror = function(e) {
                t.onError("jsonp poll error", e)
            }
            ;
            var n = document.getElementsByTagName("script")[0];
            n ? n.parentNode.insertBefore(e, n) : (document.head || document.body).appendChild(e),
            this.script = e,
            "undefined" != typeof navigator && /gecko/i.test(navigator.userAgent) && setTimeout(function() {
                var t = document.createElement("iframe");
                document.body.appendChild(t),
                document.body.removeChild(t)
            }, 100)
        }
        ,
        o.prototype.doWrite = function(t, e) {
            function n() {
                r(),
                e()
            }
            function r() {
                if (o.iframe)
                    try {
                        o.form.removeChild(o.iframe)
                    } catch (t) {
                        o.onError("jsonp polling iframe removal error", t)
                    }
                try {
                    var t = '<iframe src="javascript:0" name="' + o.iframeId + '">';
                    i = document.createElement(t)
                } catch (t) {
                    i = document.createElement("iframe"),
                    i.name = o.iframeId,
                    i.src = "javascript:0"
                }
                i.id = o.iframeId,
                o.form.appendChild(i),
                o.iframe = i
            }
            var o = this;
            if (!this.form) {
                var i, s = document.createElement("form"), a = document.createElement("textarea"), c = this.iframeId = "eio_iframe_" + this.index;
                s.className = "socketio",
                s.style.position = "absolute",
                s.style.top = "-1000px",
                s.style.left = "-1000px",
                s.target = c,
                s.method = "POST",
                s.setAttribute("accept-charset", "utf-8"),
                a.name = "d",
                s.appendChild(a),
                document.body.appendChild(s),
                this.form = s,
                this.area = a
            }
            this.form.action = this.uri(),
            r(),
            t = t.replace(f, "\\\n"),
            this.area.value = t.replace(u, "\\n");
            try {
                this.form.submit()
            } catch (t) {}
            this.iframe.attachEvent ? this.iframe.onreadystatechange = function() {
                "complete" === o.iframe.readyState && n()
            }
            : this.iframe.onload = n
        }
    }
    , function(t, e, n) {
        function r() {}
        function o(t) {
            if (c.call(this, t),
            this.requestTimeout = t.requestTimeout,
            this.extraHeaders = t.extraHeaders,
            "undefined" != typeof location) {
                var e = "https:" === location.protocol
                  , n = location.port;
                n || (n = e ? 443 : 80),
                this.xd = "undefined" != typeof location && t.hostname !== location.hostname || n !== t.port,
                this.xs = t.secure !== e
            }
        }
        function i(t) {
            this.method = t.method || "GET",
            this.uri = t.uri,
            this.xd = !!t.xd,
            this.xs = !!t.xs,
            this.async = !1 !== t.async,
            this.data = void 0 !== t.data ? t.data : null,
            this.agent = t.agent,
            this.isBinary = t.isBinary,
            this.supportsBinary = t.supportsBinary,
            this.enablesXDR = t.enablesXDR,
            this.withCredentials = t.withCredentials,
            this.requestTimeout = t.requestTimeout,
            this.pfx = t.pfx,
            this.key = t.key,
            this.passphrase = t.passphrase,
            this.cert = t.cert,
            this.ca = t.ca,
            this.ciphers = t.ciphers,
            this.rejectUnauthorized = t.rejectUnauthorized,
            this.extraHeaders = t.extraHeaders,
            this.create()
        }
        function s() {
            for (var t in i.requests)
                i.requests.hasOwnProperty(t) && i.requests[t].abort()
        }
        var a = n(10)
          , c = n(14)
          , u = n(0)
          , f = n(3)
          , h = n(4)("engine.io-client:polling-xhr")
          , p = n(8);
        if (t.exports = o,
        t.exports.Request = i,
        f(o, c),
        o.prototype.supportsBinary = !0,
        o.prototype.request = function(t) {
            return t = t || {},
            t.uri = this.uri(),
            t.xd = this.xd,
            t.xs = this.xs,
            t.agent = this.agent || !1,
            t.supportsBinary = this.supportsBinary,
            t.enablesXDR = this.enablesXDR,
            t.withCredentials = this.withCredentials,
            t.pfx = this.pfx,
            t.key = this.key,
            t.passphrase = this.passphrase,
            t.cert = this.cert,
            t.ca = this.ca,
            t.ciphers = this.ciphers,
            t.rejectUnauthorized = this.rejectUnauthorized,
            t.requestTimeout = this.requestTimeout,
            t.extraHeaders = this.extraHeaders,
            new i(t)
        }
        ,
        o.prototype.doWrite = function(t, e) {
            var n = "string" != typeof t && void 0 !== t
              , r = this.request({
                method: "POST",
                data: t,
                isBinary: n
            })
              , o = this;
            r.on("success", e),
            r.on("error", function(t) {
                o.onError("xhr post error", t)
            }),
            this.sendXhr = r
        }
        ,
        o.prototype.doPoll = function() {
            h("xhr poll");
            var t = this.request()
              , e = this;
            t.on("data", function(t) {
                e.onData(t)
            }),
            t.on("error", function(t) {
                e.onError("xhr poll error", t)
            }),
            this.pollXhr = t
        }
        ,
        u(i.prototype),
        i.prototype.create = function() {
            var t = {
                agent: this.agent,
                xdomain: this.xd,
                xscheme: this.xs,
                enablesXDR: this.enablesXDR
            };
            t.pfx = this.pfx,
            t.key = this.key,
            t.passphrase = this.passphrase,
            t.cert = this.cert,
            t.ca = this.ca,
            t.ciphers = this.ciphers,
            t.rejectUnauthorized = this.rejectUnauthorized;
            var e = this.xhr = new a(t)
              , n = this;
            try {
                h("xhr open %s: %s", this.method, this.uri),
                e.open(this.method, this.uri, this.async);
                try {
                    if (this.extraHeaders) {
                        e.setDisableHeaderCheck && e.setDisableHeaderCheck(!0);
                        for (var r in this.extraHeaders)
                            this.extraHeaders.hasOwnProperty(r) && e.setRequestHeader(r, this.extraHeaders[r])
                    }
                } catch (t) {}
                if ("POST" === this.method)
                    try {
                        this.isBinary ? e.setRequestHeader("Content-type", "application/octet-stream") : e.setRequestHeader("Content-type", "text/plain;charset=UTF-8")
                    } catch (t) {}
                try {
                    e.setRequestHeader("Accept", "*/*")
                } catch (t) {}
                "withCredentials"in e && (e.withCredentials = this.withCredentials),
                this.requestTimeout && (e.timeout = this.requestTimeout),
                this.hasXDR() ? (e.onload = function() {
                    n.onLoad()
                }
                ,
                e.onerror = function() {
                    n.onError(e.responseText)
                }
                ) : e.onreadystatechange = function() {
                    if (2 === e.readyState)
                        try {
                            var t = e.getResponseHeader("Content-Type");
                            (n.supportsBinary && "application/octet-stream" === t || "application/octet-stream; charset=UTF-8" === t) && (e.responseType = "arraybuffer")
                        } catch (t) {}
                    4 === e.readyState && (200 === e.status || 1223 === e.status ? n.onLoad() : setTimeout(function() {
                        n.onError("number" == typeof e.status ? e.status : 0)
                    }, 0))
                }
                ,
                h("xhr data %s", this.data),
                e.send(this.data)
            } catch (t) {
                return void setTimeout(function() {
                    n.onError(t)
                }, 0)
            }
            "undefined" != typeof document && (this.index = i.requestsCount++,
            i.requests[this.index] = this)
        }
        ,
        i.prototype.onSuccess = function() {
            this.emit("success"),
            this.cleanup()
        }
        ,
        i.prototype.onData = function(t) {
            this.emit("data", t),
            this.onSuccess()
        }
        ,
        i.prototype.onError = function(t) {
            this.emit("error", t),
            this.cleanup(!0)
        }
        ,
        i.prototype.cleanup = function(t) {
            if (void 0 !== this.xhr && null !== this.xhr) {
                if (this.hasXDR() ? this.xhr.onload = this.xhr.onerror = r : this.xhr.onreadystatechange = r,
                t)
                    try {
                        this.xhr.abort()
                    } catch (t) {}
                "undefined" != typeof document && delete i.requests[this.index],
                this.xhr = null
            }
        }
        ,
        i.prototype.onLoad = function() {
            var t;
            try {
                var e;
                try {
                    e = this.xhr.getResponseHeader("Content-Type")
                } catch (t) {}
                t = "application/octet-stream" === e || "application/octet-stream; charset=UTF-8" === e ? this.xhr.response || this.xhr.responseText : this.xhr.responseText
            } catch (t) {
                this.onError(t)
            }
            null != t && this.onData(t)
        }
        ,
        i.prototype.hasXDR = function() {
            return "undefined" != typeof XDomainRequest && !this.xs && this.enablesXDR
        }
        ,
        i.prototype.abort = function() {
            this.cleanup()
        }
        ,
        i.requestsCount = 0,
        i.requests = {},
        "undefined" != typeof document)
            if ("function" == typeof attachEvent)
                attachEvent("onunload", s);
            else if ("function" == typeof addEventListener) {
                var l = "onpagehide"in p ? "pagehide" : "unload";
                addEventListener(l, s, !1)
            }
    }
    , function(t, e, n) {
        (function(e) {
            function r(t) {
                t && t.forceBase64 && (this.supportsBinary = !1),
                this.perMessageDeflate = t.perMessageDeflate,
                this.usingBrowserWebSocket = o && !t.forceNode,
                this.protocols = t.protocols,
                this.usingBrowserWebSocket || (p = i),
                s.call(this, t)
            }
            var o, i, s = n(9), a = n(1), c = n(5), u = n(3), f = n(23), h = n(4)("engine.io-client:websocket");
            if ("undefined" != typeof WebSocket ? o = WebSocket : "undefined" != typeof self && (o = self.WebSocket || self.MozWebSocket),
            "undefined" == typeof window)
                try {
                    i = n(53)
                } catch (t) {}
            var p = o || i;
            t.exports = r,
            u(r, s),
            r.prototype.name = "websocket",
            r.prototype.supportsBinary = !0,
            r.prototype.doOpen = function() {
                if (this.check()) {
                    var t = this.uri()
                      , e = this.protocols
                      , n = {};
                    this.isReactNative || (n.agent = this.agent,
                    n.perMessageDeflate = this.perMessageDeflate,
                    n.pfx = this.pfx,
                    n.key = this.key,
                    n.passphrase = this.passphrase,
                    n.cert = this.cert,
                    n.ca = this.ca,
                    n.ciphers = this.ciphers,
                    n.rejectUnauthorized = this.rejectUnauthorized),
                    this.extraHeaders && (n.headers = this.extraHeaders),
                    this.localAddress && (n.localAddress = this.localAddress);
                    try {
                        this.ws = this.usingBrowserWebSocket && !this.isReactNative ? e ? new p(t,e) : new p(t) : new p(t,e,n)
                    } catch (t) {
                        return this.emit("error", t)
                    }
                    void 0 === this.ws.binaryType && (this.supportsBinary = !1),
                    this.ws.supports && this.ws.supports.binary ? (this.supportsBinary = !0,
                    this.ws.binaryType = "nodebuffer") : this.ws.binaryType = "arraybuffer",
                    this.addEventListeners()
                }
            }
            ,
            r.prototype.addEventListeners = function() {
                var t = this;
                this.ws.onopen = function() {
                    t.onOpen()
                }
                ,
                this.ws.onclose = function() {
                    t.onClose()
                }
                ,
                this.ws.onmessage = function(e) {
                    t.onData(e.data)
                }
                ,
                this.ws.onerror = function(e) {
                    t.onError("websocket error", e)
                }
            }
            ,
            r.prototype.write = function(t) {
                function n() {
                    r.emit("flush"),
                    setTimeout(function() {
                        r.writable = !0,
                        r.emit("drain")
                    }, 0)
                }
                var r = this;
                this.writable = !1;
                for (var o = t.length, i = 0, s = o; i < s; i++)
                    !function(t) {
                        a.encodePacket(t, r.supportsBinary, function(i) {
                            if (!r.usingBrowserWebSocket) {
                                var s = {};
                                if (t.options && (s.compress = t.options.compress),
                                r.perMessageDeflate) {
                                    ("string" == typeof i ? e.byteLength(i) : i.length) < r.perMessageDeflate.threshold && (s.compress = !1)
                                }
                            }
                            try {
                                r.usingBrowserWebSocket ? r.ws.send(i) : r.ws.send(i, s)
                            } catch (t) {
                                h("websocket closed before onclose event")
                            }
                            --o || n()
                        })
                    }(t[i])
            }
            ,
            r.prototype.onClose = function() {
                s.prototype.onClose.call(this)
            }
            ,
            r.prototype.doClose = function() {
                void 0 !== this.ws && this.ws.close()
            }
            ,
            r.prototype.uri = function() {
                var t = this.query || {}
                  , e = this.secure ? "wss" : "ws"
                  , n = "";
                return this.port && ("wss" === e && 443 !== Number(this.port) || "ws" === e && 80 !== Number(this.port)) && (n = ":" + this.port),
                this.timestampRequests && (t[this.timestampParam] = f()),
                this.supportsBinary || (t.b64 = 1),
                t = c.encode(t),
                t.length && (t = "?" + t),
                e + "://" + (-1 !== this.hostname.indexOf(":") ? "[" + this.hostname + "]" : this.hostname) + n + this.path + t
            }
            ,
            r.prototype.check = function() {
                return !(!p || "__initialize"in p && this.name === r.prototype.name)
            }
        }
        ).call(e, n(7).Buffer)
    }
    , function(t, e, n) {
        function r(t) {
            var n, r = 0;
            for (n in t)
                r = (r << 5) - r + t.charCodeAt(n),
                r |= 0;
            return e.colors[Math.abs(r) % e.colors.length]
        }
        function o(t) {
            function n() {
                if (n.enabled) {
                    var t = n
                      , r = +new Date
                      , i = r - (o || r);
                    t.diff = i,
                    t.prev = o,
                    t.curr = r,
                    o = r;
                    for (var s = new Array(arguments.length), a = 0; a < s.length; a++)
                        s[a] = arguments[a];
                    s[0] = e.coerce(s[0]),
                    "string" != typeof s[0] && s.unshift("%O");
                    var c = 0;
                    s[0] = s[0].replace(/%([a-zA-Z%])/g, function(n, r) {
                        if ("%%" === n)
                            return n;
                        c++;
                        var o = e.formatters[r];
                        if ("function" == typeof o) {
                            var i = s[c];
                            n = o.call(t, i),
                            s.splice(c, 1),
                            c--
                        }
                        return n
                    }),
                    e.formatArgs.call(t, s);
                    (n.log || e.log || console.log.bind(console)).apply(t, s)
                }
            }
            var o;
            return n.namespace = t,
            n.enabled = e.enabled(t),
            n.useColors = e.useColors(),
            n.color = r(t),
            n.destroy = i,
            "function" == typeof e.init && e.init(n),
            e.instances.push(n),
            n
        }
        function i() {
            var t = e.instances.indexOf(this);
            return -1 !== t && (e.instances.splice(t, 1),
            !0)
        }
        function s(t) {
            e.save(t),
            e.names = [],
            e.skips = [];
            var n, r = ("string" == typeof t ? t : "").split(/[\s,]+/), o = r.length;
            for (n = 0; n < o; n++)
                r[n] && (t = r[n].replace(/\*/g, ".*?"),
                "-" === t[0] ? e.skips.push(new RegExp("^" + t.substr(1) + "$")) : e.names.push(new RegExp("^" + t + "$")));
            for (n = 0; n < e.instances.length; n++) {
                var i = e.instances[n];
                i.enabled = e.enabled(i.namespace)
            }
        }
        function a() {
            e.enable("")
        }
        function c(t) {
            if ("*" === t[t.length - 1])
                return !0;
            var n, r;
            for (n = 0,
            r = e.skips.length; n < r; n++)
                if (e.skips[n].test(t))
                    return !1;
            for (n = 0,
            r = e.names.length; n < r; n++)
                if (e.names[n].test(t))
                    return !0;
            return !1
        }
        function u(t) {
            return t instanceof Error ? t.stack || t.message : t
        }
        e = t.exports = o.debug = o.default = o,
        e.coerce = u,
        e.disable = a,
        e.enable = s,
        e.enabled = c,
        e.humanize = n(40),
        e.instances = [],
        e.names = [],
        e.skips = [],
        e.formatters = {}
    }
    , function(t, e) {
        function n(t) {
            if (t = String(t),
            !(t.length > 100)) {
                var e = /^((?:\d+)?\.?\d+) *(milliseconds?|msecs?|ms|seconds?|secs?|s|minutes?|mins?|m|hours?|hrs?|h|days?|d|years?|yrs?|y)?$/i.exec(t);
                if (e) {
                    var n = parseFloat(e[1]);
                    switch ((e[2] || "ms").toLowerCase()) {
                    case "years":
                    case "year":
                    case "yrs":
                    case "yr":
                    case "y":
                        return n * f;
                    case "days":
                    case "day":
                    case "d":
                        return n * u;
                    case "hours":
                    case "hour":
                    case "hrs":
                    case "hr":
                    case "h":
                        return n * c;
                    case "minutes":
                    case "minute":
                    case "mins":
                    case "min":
                    case "m":
                        return n * a;
                    case "seconds":
                    case "second":
                    case "secs":
                    case "sec":
                    case "s":
                        return n * s;
                    case "milliseconds":
                    case "millisecond":
                    case "msecs":
                    case "msec":
                    case "ms":
                        return n;
                    default:
                        return
                    }
                }
            }
        }
        function r(t) {
            return t >= u ? Math.round(t / u) + "d" : t >= c ? Math.round(t / c) + "h" : t >= a ? Math.round(t / a) + "m" : t >= s ? Math.round(t / s) + "s" : t + "ms"
        }
        function o(t) {
            return i(t, u, "day") || i(t, c, "hour") || i(t, a, "minute") || i(t, s, "second") || t + " ms"
        }
        function i(t, e, n) {
            if (!(t < e))
                return t < 1.5 * e ? Math.floor(t / e) + " " + n : Math.ceil(t / e) + " " + n + "s"
        }
        var s = 1e3
          , a = 60 * s
          , c = 60 * a
          , u = 24 * c
          , f = 365.25 * u;
        t.exports = function(t, e) {
            e = e || {};
            var i = typeof t;
            if ("string" === i && t.length > 0)
                return n(t);
            if ("number" === i && !1 === isNaN(t))
                return e.long ? o(t) : r(t);
            throw new Error("val is not a non-empty string or a valid number. val=" + JSON.stringify(t))
        }
    }
    , function(t, e) {
        t.exports = Object.keys || function(t) {
            var e = []
              , n = Object.prototype.hasOwnProperty;
            for (var r in t)
                n.call(t, r) && e.push(r);
            return e
        }
    }
    , function(t, e) {
        function n(t) {
            for (var e, n, r = [], o = 0, i = t.length; o < i; )
                e = t.charCodeAt(o++),
                e >= 55296 && e <= 56319 && o < i ? (n = t.charCodeAt(o++),
                56320 == (64512 & n) ? r.push(((1023 & e) << 10) + (1023 & n) + 65536) : (r.push(e),
                o--)) : r.push(e);
            return r
        }
        function r(t) {
            for (var e, n = t.length, r = -1, o = ""; ++r < n; )
                e = t[r],
                e > 65535 && (e -= 65536,
                o += d(e >>> 10 & 1023 | 55296),
                e = 56320 | 1023 & e),
                o += d(e);
            return o
        }
        function o(t, e) {
            if (t >= 55296 && t <= 57343) {
                if (e)
                    throw Error("Lone surrogate U+" + t.toString(16).toUpperCase() + " is not a scalar value");
                return !1
            }
            return !0
        }
        function i(t, e) {
            return d(t >> e & 63 | 128)
        }
        function s(t, e) {
            if (0 == (4294967168 & t))
                return d(t);
            var n = "";
            return 0 == (4294965248 & t) ? n = d(t >> 6 & 31 | 192) : 0 == (4294901760 & t) ? (o(t, e) || (t = 65533),
            n = d(t >> 12 & 15 | 224),
            n += i(t, 6)) : 0 == (4292870144 & t) && (n = d(t >> 18 & 7 | 240),
            n += i(t, 12),
            n += i(t, 6)),
            n += d(63 & t | 128)
        }
        function a(t, e) {
            e = e || {};
            for (var r, o = !1 !== e.strict, i = n(t), a = i.length, c = -1, u = ""; ++c < a; )
                r = i[c],
                u += s(r, o);
            return u
        }
        function c() {
            if (l >= p)
                throw Error("Invalid byte index");
            var t = 255 & h[l];
            if (l++,
            128 == (192 & t))
                return 63 & t;
            throw Error("Invalid continuation byte")
        }
        function u(t) {
            var e, n, r, i, s;
            if (l > p)
                throw Error("Invalid byte index");
            if (l == p)
                return !1;
            if (e = 255 & h[l],
            l++,
            0 == (128 & e))
                return e;
            if (192 == (224 & e)) {
                if (n = c(),
                (s = (31 & e) << 6 | n) >= 128)
                    return s;
                throw Error("Invalid continuation byte")
            }
            if (224 == (240 & e)) {
                if (n = c(),
                r = c(),
                (s = (15 & e) << 12 | n << 6 | r) >= 2048)
                    return o(s, t) ? s : 65533;
                throw Error("Invalid continuation byte")
            }
            if (240 == (248 & e) && (n = c(),
            r = c(),
            i = c(),
            (s = (7 & e) << 18 | n << 12 | r << 6 | i) >= 65536 && s <= 1114111))
                return s;
            throw Error("Invalid UTF-8 detected")
        }
        function f(t, e) {
            e = e || {};
            var o = !1 !== e.strict;
            h = n(t),
            p = h.length,
            l = 0;
            for (var i, s = []; !1 !== (i = u(o)); )
                s.push(i);
            return r(s)
        }
        /*! https://mths.be/utf8js v2.1.2 by @mathias */
        var h, p, l, d = String.fromCharCode;
        t.exports = {
            version: "2.1.2",
            encode: a,
            decode: f
        }
    }
    , function(t, e) {
        var n = {}.toString;
        t.exports = Array.isArray || function(t) {
            return "[object Array]" == n.call(t)
        }
    }
    , function(t, e) {
        try {
            t.exports = "undefined" != typeof XMLHttpRequest && "withCredentials"in new XMLHttpRequest
        } catch (e) {
            t.exports = !1
        }
    }
    , function(t, e) {
        /*! ieee754. BSD-3-Clause License. Feross Aboukhadijeh <https://feross.org/opensource> */
        e.read = function(t, e, n, r, o) {
            var i, s, a = 8 * o - r - 1, c = (1 << a) - 1, u = c >> 1, f = -7, h = n ? o - 1 : 0, p = n ? -1 : 1, l = t[e + h];
            for (h += p,
            i = l & (1 << -f) - 1,
            l >>= -f,
            f += a; f > 0; i = 256 * i + t[e + h],
            h += p,
            f -= 8)
                ;
            for (s = i & (1 << -f) - 1,
            i >>= -f,
            f += r; f > 0; s = 256 * s + t[e + h],
            h += p,
            f -= 8)
                ;
            if (0 === i)
                i = 1 - u;
            else {
                if (i === c)
                    return s ? NaN : 1 / 0 * (l ? -1 : 1);
                s += Math.pow(2, r),
                i -= u
            }
            return (l ? -1 : 1) * s * Math.pow(2, i - r)
        }
        ,
        e.write = function(t, e, n, r, o, i) {
            var s, a, c, u = 8 * i - o - 1, f = (1 << u) - 1, h = f >> 1, p = 23 === o ? Math.pow(2, -24) - Math.pow(2, -77) : 0, l = r ? 0 : i - 1, d = r ? 1 : -1, y = e < 0 || 0 === e && 1 / e < 0 ? 1 : 0;
            for (e = Math.abs(e),
            isNaN(e) || e === 1 / 0 ? (a = isNaN(e) ? 1 : 0,
            s = f) : (s = Math.floor(Math.log(e) / Math.LN2),
            e * (c = Math.pow(2, -s)) < 1 && (s--,
            c *= 2),
            e += s + h >= 1 ? p / c : p * Math.pow(2, 1 - h),
            e * c >= 2 && (s++,
            c /= 2),
            s + h >= f ? (a = 0,
            s = f) : s + h >= 1 ? (a = (e * c - 1) * Math.pow(2, o),
            s += h) : (a = e * Math.pow(2, h - 1) * Math.pow(2, o),
            s = 0)); o >= 8; t[n + l] = 255 & a,
            l += d,
            a /= 256,
            o -= 8)
                ;
            for (s = s << o | a,
            u += o; u > 0; t[n + l] = 255 & s,
            l += d,
            s /= 256,
            u -= 8)
                ;
            t[n + l - d] |= 128 * y
        }
    }
    , function(t, e) {
        var n = {}.toString;
        t.exports = Array.isArray || function(t) {
            return "[object Array]" == n.call(t)
        }
    }
    , function(t, e, n) {
        function r(t, e) {
            var n = t;
            e = e || "undefined" != typeof location && location,
            null == t && (t = e.protocol + "//" + e.host),
            "string" == typeof t && ("/" === t.charAt(0) && (t = "/" === t.charAt(1) ? e.protocol + t : e.host + t),
            /^(https?|wss?):\/\//.test(t) || (i("protocol-less url %s", t),
            t = void 0 !== e ? e.protocol + "//" + t : "https://" + t),
            i("parse %s", t),
            n = o(t)),
            n.port || (/^(http|ws)$/.test(n.protocol) ? n.port = "80" : /^(http|ws)s$/.test(n.protocol) && (n.port = "443")),
            n.path = n.path || "/";
            var r = -1 !== n.host.indexOf(":")
              , s = r ? "[" + n.host + "]" : n.host;
            return n.id = n.protocol + "://" + s + ":" + n.port,
            n.href = n.protocol + "://" + s + (e && e.port === n.port ? "" : ":" + n.port),
            n
        }
        var o = n(17)
          , i = n(2)("socket.io-client:url");
        t.exports = r
    }
    , function(t, e, n) {
        function r(t) {
            var n, r = 0;
            for (n in t)
                r = (r << 5) - r + t.charCodeAt(n),
                r |= 0;
            return e.colors[Math.abs(r) % e.colors.length]
        }
        function o(t) {
            function n() {
                if (n.enabled) {
                    var t = n
                      , r = +new Date
                      , i = r - (o || r);
                    t.diff = i,
                    t.prev = o,
                    t.curr = r,
                    o = r;
                    for (var s = new Array(arguments.length), a = 0; a < s.length; a++)
                        s[a] = arguments[a];
                    s[0] = e.coerce(s[0]),
                    "string" != typeof s[0] && s.unshift("%O");
                    var c = 0;
                    s[0] = s[0].replace(/%([a-zA-Z%])/g, function(n, r) {
                        if ("%%" === n)
                            return n;
                        c++;
                        var o = e.formatters[r];
                        if ("function" == typeof o) {
                            var i = s[c];
                            n = o.call(t, i),
                            s.splice(c, 1),
                            c--
                        }
                        return n
                    }),
                    e.formatArgs.call(t, s);
                    (n.log || e.log || console.log.bind(console)).apply(t, s)
                }
            }
            var o;
            return n.namespace = t,
            n.enabled = e.enabled(t),
            n.useColors = e.useColors(),
            n.color = r(t),
            n.destroy = i,
            "function" == typeof e.init && e.init(n),
            e.instances.push(n),
            n
        }
        function i() {
            var t = e.instances.indexOf(this);
            return -1 !== t && (e.instances.splice(t, 1),
            !0)
        }
        function s(t) {
            e.save(t),
            e.names = [],
            e.skips = [];
            var n, r = ("string" == typeof t ? t : "").split(/[\s,]+/), o = r.length;
            for (n = 0; n < o; n++)
                r[n] && (t = r[n].replace(/\*/g, ".*?"),
                "-" === t[0] ? e.skips.push(new RegExp("^" + t.substr(1) + "$")) : e.names.push(new RegExp("^" + t + "$")));
            for (n = 0; n < e.instances.length; n++) {
                var i = e.instances[n];
                i.enabled = e.enabled(i.namespace)
            }
        }
        function a() {
            e.enable("")
        }
        function c(t) {
            if ("*" === t[t.length - 1])
                return !0;
            var n, r;
            for (n = 0,
            r = e.skips.length; n < r; n++)
                if (e.skips[n].test(t))
                    return !1;
            for (n = 0,
            r = e.names.length; n < r; n++)
                if (e.names[n].test(t))
                    return !0;
            return !1
        }
        function u(t) {
            return t instanceof Error ? t.stack || t.message : t
        }
        e = t.exports = o.debug = o.default = o,
        e.coerce = u,
        e.disable = a,
        e.enable = s,
        e.enabled = c,
        e.humanize = n(49),
        e.instances = [],
        e.names = [],
        e.skips = [],
        e.formatters = {}
    }
    , function(t, e) {
        function n(t) {
            if (t = String(t),
            !(t.length > 100)) {
                var e = /^((?:\d+)?\.?\d+) *(milliseconds?|msecs?|ms|seconds?|secs?|s|minutes?|mins?|m|hours?|hrs?|h|days?|d|years?|yrs?|y)?$/i.exec(t);
                if (e) {
                    var n = parseFloat(e[1]);
                    switch ((e[2] || "ms").toLowerCase()) {
                    case "years":
                    case "year":
                    case "yrs":
                    case "yr":
                    case "y":
                        return n * f;
                    case "days":
                    case "day":
                    case "d":
                        return n * u;
                    case "hours":
                    case "hour":
                    case "hrs":
                    case "hr":
                    case "h":
                        return n * c;
                    case "minutes":
                    case "minute":
                    case "mins":
                    case "min":
                    case "m":
                        return n * a;
                    case "seconds":
                    case "second":
                    case "secs":
                    case "sec":
                    case "s":
                        return n * s;
                    case "milliseconds":
                    case "millisecond":
                    case "msecs":
                    case "msec":
                    case "ms":
                        return n;
                    default:
                        return
                    }
                }
            }
        }
        function r(t) {
            return t >= u ? Math.round(t / u) + "d" : t >= c ? Math.round(t / c) + "h" : t >= a ? Math.round(t / a) + "m" : t >= s ? Math.round(t / s) + "s" : t + "ms"
        }
        function o(t) {
            return i(t, u, "day") || i(t, c, "hour") || i(t, a, "minute") || i(t, s, "second") || t + " ms"
        }
        function i(t, e, n) {
            if (!(t < e))
                return t < 1.5 * e ? Math.floor(t / e) + " " + n : Math.ceil(t / e) + " " + n + "s"
        }
        var s = 1e3
          , a = 60 * s
          , c = 60 * a
          , u = 24 * c
          , f = 365.25 * u;
        t.exports = function(t, e) {
            e = e || {};
            var i = typeof t;
            if ("string" === i && t.length > 0)
                return n(t);
            if ("number" === i && !1 === isNaN(t))
                return e.long ? o(t) : r(t);
            throw new Error("val is not a non-empty string or a valid number. val=" + JSON.stringify(t))
        }
    }
    , function(t, e, n) {
        function r(t, e) {
            if (!t)
                return t;
            if (s(t)) {
                var n = {
                    _placeholder: !0,
                    num: e.length
                };
                return e.push(t),
                n
            }
            if (i(t)) {
                for (var o = new Array(t.length), a = 0; a < t.length; a++)
                    o[a] = r(t[a], e);
                return o
            }
            if ("object" == typeof t && !(t instanceof Date)) {
                var o = {};
                for (var c in t)
                    o[c] = r(t[c], e);
                return o
            }
            return t
        }
        function o(t, e) {
            if (!t)
                return t;
            if (t && !0 === t._placeholder) {
                if ("number" == typeof t.num && t.num >= 0 && t.num < e.length)
                    return e[t.num];
                throw new Error("illegal attachments")
            }
            if (i(t))
                for (var n = 0; n < t.length; n++)
                    t[n] = o(t[n], e);
            else if ("object" == typeof t)
                for (var r in t)
                    t[r] = o(t[r], e);
            return t
        }
        var i = n(21)
          , s = n(22)
          , a = Object.prototype.toString
          , c = "function" == typeof Blob || "undefined" != typeof Blob && "[object BlobConstructor]" === a.call(Blob)
          , u = "function" == typeof File || "undefined" != typeof File && "[object FileConstructor]" === a.call(File);
        e.deconstructPacket = function(t) {
            var e = []
              , n = t.data
              , o = t;
            return o.data = r(n, e),
            o.attachments = e.length,
            {
                packet: o,
                buffers: e
            }
        }
        ,
        e.reconstructPacket = function(t, e) {
            return t.data = o(t.data, e),
            t.attachments = void 0,
            t
        }
        ,
        e.removeBlobs = function(t, e) {
            function n(t, a, f) {
                if (!t)
                    return t;
                if (c && t instanceof Blob || u && t instanceof File) {
                    r++;
                    var h = new FileReader;
                    h.onload = function() {
                        f ? f[a] = this.result : o = this.result,
                        --r || e(o)
                    }
                    ,
                    h.readAsArrayBuffer(t)
                } else if (i(t))
                    for (var p = 0; p < t.length; p++)
                        n(t[p], p, t);
                else if ("object" == typeof t && !s(t))
                    for (var l in t)
                        n(t[l], l, t)
            }
            var r = 0
              , o = t;
            n(o),
            r || e(o)
        }
    }
    , function(t, e) {
        function n(t, e) {
            var n = [];
            e = e || 0;
            for (var r = e || 0; r < t.length; r++)
                n[r - e] = t[r];
            return n
        }
        t.exports = n
    }
    , function(t, e) {
        var n;
        n = function() {
            return this
        }();
        try {
            n = n || Function("return this")() || (0,
            eval)("this")
        } catch (t) {
            "object" == typeof window && (n = window)
        }
        t.exports = n
    }
    , function(t, e) {}
    , function(t, e, n) {
        "use strict";
        (function(e) {
            var r = "function" == typeof Symbol && "symbol" == typeof Symbol.iterator ? function(t) {
                return typeof t
            }
            : function(t) {
                return t && "function" == typeof Symbol && t.constructor === Symbol && t !== Symbol.prototype ? "symbol" : typeof t
            }
              , o = n(26)
              , i = n(27)
              , s = n(25)
              , a = n(24)
              , c = window.console ? window.console.info.bind(window.console, "[rt]") : function() {}
              , u = function t(e, n, o) {
                if (null == e)
                    return "";
                var i = ""
                  , s = void 0 === e ? "undefined" : r(e);
                if ("string" == s || "number" == s || "boolean" == s)
                    i += "&" + n + "=" + (null == o || o ? encodeURIComponent(e) : e);
                else
                    for (var a in e) {
                        var c = null == n ? a : n + (e instanceof Array ? "[" + a + "]" : "." + a);
                        i += t(e[a], c, o)
                    }
                return i
            }
              , f = function(t, e, n, r) {
                var o = navigator.userAgent.toLowerCase()
                  , i = {
                    code: n,
                    msg: r,
                    openid: t,
                    url: window.location.href,
                    os: o,
                    client: e
                }
                  , s = u(i).slice(1)
                  , a = "/upcstatic/error/sock.gif?" + s;
                try {
                    fetch(a, {
                        credentials: "same-origin"
                    }).catch(function(t) {
                        c("err for report socket", t)
                    })
                } catch (t) {
                    c("err for report socket", t)
                }
            }
              , h = function(t, n) {
                var r = arguments.length > 2 && void 0 !== arguments[2] ? arguments[2] : e.env.SOCKET_SERVER;
                return o.connect((r || "") + "/userActivities", {
                    transports: ["websocket", "polling"],
                    path: "/unipusio",
                    "reconnection delay": 0,
                    "reopen delay": 0,
                    "force new connection": !0,
                    query: {
                        uuid: t,
                        token: n
                    }
                })
            }
              , p = "STATE_READY"
              , l = "STATE_CONNECT"
              , d = "STATE_START"
              , y = "STATE_STOP"
              , g = "STATE_ERROR"
              , m = null
              , v = function(t, e, n) {
                return m ? (m.start(Object.assign({
                    client: t.client
                }, e), function(t) {
                    if (c("re-instance again end "),
                    t)
                        return void c("error of re-instance again: ", t)
                }),
                m) : m = b(t, e, n)
            }
              , b = function(t, e) {
                var n = t.openId
                  , r = t.wsURL
                  , o = t.token
                  , u = t.timeout
                  , v = void 0 === u ? 60 : u
                  , b = t.client
                  , w = e.module
                  , C = e.moduleGroup
                  , A = e.url
                  , E = e.tag1
                  , x = e.tag2
                  , k = e.tag3
                  , B = arguments.length > 2 && void 0 !== arguments[2] ? arguments[2] : s
                  , T = {
                    module: w,
                    moduleGroup: C,
                    client: b,
                    url: A,
                    tag1: E,
                    tag2: x,
                    tag3: k
                }
                  , R = p
                  , _ = null
                  , S = new Date
                  , F = new i
                  , P = h(n, o, r)
                  , O = null
                  , L = !0
                  , U = function() {
                    if (c(" ***** realtime distconnect toast ******"),
                    !O && L) {
                        0 == document.getElementsByClassName("layui-layer layui-anim layui-layer-page layui-layer-rim").length && a && a("当前网络不给力，学习时长可能会记录不全，请检查网络", 3e3),
                        O = setInterval(function() {
                            0 == document.getElementsByClassName("layui-layer layui-anim layui-layer-page layui-layer-rim").length && a && a("当前网络不给力，学习时长可能会记录不全，请检查网络", 3e3),
                            c(" ***** realtime 断开连接报错 ******")
                        }, 6e4),
                        F.on("connect", N)
                    }
                }
                  , N = function t() {
                    O && (clearInterval(O),
                    O = null,
                    F.off("connect", t))
                }
                  , D = function() {
                    _ && (clearTimeout(_),
                    _ = null)
                }
                  , I = function() {
                    return S = new Date
                }
                  , j = !1;
                F.on("stop_auto", function() {
                    return j ? void c("timeout dialog has showed") : (j = !0,
                    B instanceof Function ? void B(void 0, function() {
                        j = !1,
                        I(),
                        setTimeout(z, 1e3)
                    }) : "string" == typeof B ? void s(B, function() {
                        j = !1,
                        I(),
                        setTimeout(z, 1e3)
                    }) : void 0)
                });
                var M = function() {
                    c("auto stop: state=", R),
                    R == d && H(function(t) {
                        if (c("auto stop complete"),
                        F.trigger("stop_auto"),
                        t)
                            return void c("auto stop error,", t)
                    })
                }
                  , q = function() {
                    var t = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : v;
                    return function() {
                        D();
                        var e = new Date - (S || new Date)
                          , n = 1e3 * (sessionStorage.__DEBUG_TIMEOUT || t) - e;
                        c("auto stop: in ", 1e3 * sessionStorage.__DEBUG_TIMEOUT || n, "(from " + S + ") seconds set to ", t),
                        _ = setTimeout(M, 1e3 * sessionStorage.__DEBUG_TIMEOUT || (n > 0 ? n : 0))
                    }()
                }
                  , Y = function(t, e) {
                    var r = t || {}
                      , o = r.module
                      , i = r.moduleGroup;
                    r.client,
                    r.url,
                    r.tag1,
                    r.tag2,
                    r.tag3;
                    return o ? n ? i ? void e(null, t) : void e(new Error("moduleGroup expect not null")) : void e(new Error("openId expect not null")) : void e(new Error("module expect not null"))
                }
                  , z = function(t) {
                    var e = arguments.length > 1 && void 0 !== arguments[1] ? arguments[1] : T;
                    Y(e, function(n) {
                        if (n)
                            return void (t && t(n));
                        var r = (new Date).getTime()
                          , o = Object.assign({}, e, {
                            timer: r
                        });
                        if (T = o,
                        !(R == l || R == y || R == d))
                            return void c("state is not STATE_CONNECT, but: ", R, " will start on reconnect");
                        c("emit start: ", o),
                        D(),
                        P.emit("start", o, function(e) {
                            var n = e || {
                                code: 4,
                                msg: "dont get from server"
                            }
                              , r = n.code
                              , o = n.msg;
                            if (c("response. rdata", e),
                            r)
                                return R = g,
                                F.trigger("start_error", {
                                    code: r,
                                    msg: o
                                }),
                                void (t && t(new Error({
                                    code: r,
                                    msg: o
                                })));
                            R = d,
                            q(),
                            F.trigger("start", {
                                code: r,
                                msg: o
                            })
                        })
                    })
                }
                  , H = function(t) {
                    var e = T || {}
                      , n = (new Date).getTime()
                      , r = Object.assign({}, e, {
                        timer: n
                    });
                    P.emit("stop", r, function(e) {
                        var n = e || {
                            code: 4,
                            msg: "dont get from server"
                        }
                          , r = n.code
                          , o = n.msg;
                        if (c("stop response. rdata", e),
                        r)
                            return R = g,
                            F.trigger("stop_error", {
                                code: r,
                                msg: o
                            }),
                            void (t && t(new Error({
                                code: r,
                                msg: o
                            })));
                        R = y,
                        D(),
                        F.trigger("stop", {
                            code: r,
                            msg: o
                        }),
                        t && t()
                    })
                };
                return P.on("disconnect", function() {
                    c("disconnected..."),
                    f(n, b, 101, "disconnect connection"),
                    R = p,
                    D(),
                    U(),
                    F.trigger("disconnect")
                }),
                P.on("connect", function() {
                    c("==connect=>>>>"),
                    R = l,
                    F.trigger("connect"),
                    w && (c("module set on init, auto start."),
                    z(function(t) {
                        t && c("start error", t)
                    }))
                }),
                P.on("error", function(t) {
                    c("on error...", t),
                    f(n, b, 301, t),
                    F.trigger("realtime-log-error", {
                        msg: "realtime"
                    })
                }),
                P.on("connect_error", function(t) {
                    c("error...", t),
                    F.trigger("realtime-log-error", {
                        msg: "realtime"
                    }),
                    f(n, b, 101, t),
                    D(),
                    U(),
                    F.trigger("connect_error", t)
                }),
                {
                    start: function(t, e) {
                        I(),
                        L = !0,
                        z(e, t)
                    },
                    stop: function(t) {
                        L = !1,
                        H(t)
                    },
                    on: function(t, e) {
                        F.on(t, e)
                    },
                    off: function(t, e) {
                        F.off(t, e)
                    },
                    get state() {
                        return R
                    },
                    revived: function(t) {
                        I(),
                        q(t)
                    },
                    destroy: function(t) {
                        H(function() {
                            m = null,
                            t && t()
                        })
                    }
                }
            };
            t.exports = {
                configTimeline: v
            }
        }
        ).call(e, n(6))
    }
    ])
});
//# sourceMappingURL=realtime-sdk.js.map
