import { Injectable } from '@angular/core';
import { Subject } from 'rxjs/internal/Subject';

@Injectable({
  providedIn: 'root'
})
export class AuthSyncService {

  private channel = new BroadcastChannel('auth-channel');

  private tokenReceivedSubject = new Subject<string>();

  tokenReceived$ = this.tokenReceivedSubject.asObservable();

  private loginSubject = new Subject<string>();

  login$ = this.loginSubject.asObservable();

  private logoutSubject = new Subject<void>();

  logout$ = this.logoutSubject.asObservable();

  constructor() {

    this.channel.onmessage = (event) => {

      switch (event.data.type) {

        case 'REQUEST_TOKEN':

          const token = sessionStorage.getItem('token');

          if (token) {
            this.channel.postMessage({
              type: 'TOKEN_RESPONSE',
              token
            });
          }

          break;

        case 'TOKEN_RESPONSE':

          this.tokenReceivedSubject.next(event.data.token);

          break;
        
         case 'LOGIN':

          if (!sessionStorage.getItem('token')) {

            sessionStorage.setItem('token', event.data.token);

            this.loginSubject.next(event.data.token);
          }

          break; 

        case 'LOGOUT':

          sessionStorage.clear();
          this.logoutSubject.next();
          break;
      }
    };
  }

  requestToken() {
    this.channel.postMessage({
      type: 'REQUEST_TOKEN'
    });
  }
  
  broadcastLogin(token: string) {
  this.channel.postMessage({
    type: 'LOGIN',
    token
  });
}

  broadcastSeletiveLogout(){
    
  }

  broadcastLogout() {
    this.channel.postMessage({
      type: 'LOGOUT'
    });
  }
}