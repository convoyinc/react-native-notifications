"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const Notification_1 = require("./Notification");
const _ = require("lodash");
class NotificationIOS extends Notification_1.Notification {
    constructor(payload) {
        super(payload);
        this.identifier = this.payload.identifier;
    }
    get aps() {
        return this.payload.aps || {};
    }
    get alert() {
        if (_.isObject(this.aps.alert)) {
            return this.aps.alert;
        }
        else if (_.isString(this.aps.alert)) {
            return {
                body: this.aps.alert
            };
        }
    }
    get title() {
        if (!this.aps || !this.alert) {
            return super.title;
        }
        return this.alert.title;
    }
    get body() {
        if (!this.aps || !this.alert) {
            return super.body;
        }
        return this.alert.body;
    }
    get sound() {
        if (!this.aps) {
            return super.sound;
        }
        return this.aps.sound;
    }
    get badge() {
        if (!this.aps) {
            return super.badge;
        }
        return this.aps.badge;
    }
    get thread() {
        if (!this.aps) {
            return super.thread;
        }
        return this.aps.thread;
    }
}
exports.NotificationIOS = NotificationIOS;
