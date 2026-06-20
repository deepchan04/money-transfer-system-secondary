import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';

export const authGuard: CanActivateFn = (route, state) => {
    const router = inject(Router);
    const token = sessionStorage.getItem('token');
    const user = sessionStorage.getItem('user');
    let userData;
    if(user){
        userData = JSON.parse(user);        
    }

    if (userData.role === "ROLE_ADMIN" || token && userData.appStatus !== "CLOSED") {
        return true;
    } else {
        router.navigate(['/auth']);
        return false;
    }
};
