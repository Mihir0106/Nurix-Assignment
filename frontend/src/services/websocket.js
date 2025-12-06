import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

class WebSocketService {
    constructor() {
        this.stompClient = null;
        this.subscribers = new Map();
        this.connected = false;
        this.reconnectTimeout = null;
    }

    connect(onConnect) {
        if (this.stompClient && this.stompClient.connected) {
            return;
        }

        const socket = new SockJS('http://localhost:8080/ws');
        this.stompClient = Stomp.over(socket);
        this.stompClient.debug = () => { }; // Disable debug logs

        this.stompClient.connect({}, (frame) => {
            console.log('Connected: ' + frame);
            this.connected = true;
            if (onConnect) onConnect();

            // Resubscribe if needed
            this.subscribers.forEach((callback, topic) => {
                this._subscribeInternal(topic, callback);
            });

        }, (error) => {
            console.error('STOMP error', error);
            this.connected = false;
            this.scheduleReconnect();
        });
    }

    scheduleReconnect() {
        if (this.reconnectTimeout) return;
        this.reconnectTimeout = setTimeout(() => {
            console.log('Reconnecting...');
            this.reconnectTimeout = null;
            this.connect();
        }, 5000);
    }

    subscribe(topic, callback) {
        this.subscribers.set(topic, callback);
        if (this.connected) {
            this._subscribeInternal(topic, callback);
        }
    }

    _subscribeInternal(topic, callback) {
        this.stompClient.subscribe(topic, (message) => {
            callback(JSON.parse(message.body));
        });
    }

    disconnect() {
        if (this.stompClient) {
            this.stompClient.disconnect();
        }
    }
}

export const webSocketService = new WebSocketService();
