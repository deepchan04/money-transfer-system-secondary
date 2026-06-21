import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ServerStatusService {

  private serverDownSubject = new BehaviorSubject(false);

  serverDown$ = this.serverDownSubject.asObservable();

  setServerDown(status: boolean): void {
    this.serverDownSubject.next(status);
  }
}