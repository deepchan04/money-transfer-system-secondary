import { inject } from '@angular/core';
import {
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';
import { catchError, tap, throwError } from 'rxjs';
import { ServerStatusService } from '../service/server-status.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {

  const serverStatus = inject(ServerStatusService);

  return next(req).pipe(

    tap(() => serverStatus.setServerDown(false)),

    catchError((error: HttpErrorResponse) => {

      if (error.status === 0) {
        console.log("SETTING SERVER DOWN");
        serverStatus.setServerDown(true);
      }

      return throwError(() => error);
    })
  );
};