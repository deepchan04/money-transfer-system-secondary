import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Router, CanActivateFn } from '@angular/router';

export const closedAccount: CanActivateFn = (route, state) => {
    const router = inject(Router);
    const authService = inject(AuthService);
  const user = authService.getCurrentUser();
    let userData;
    if(user){
        userData = user;        
    }

    if (userData.role === "ROLE_ADMIN" || userData.appStatus === "ACTIVE") {
        return true;
    } else {
        router.navigate(['/auth']);
        return false;
    }
};
