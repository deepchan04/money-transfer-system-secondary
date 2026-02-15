import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';

export const closedAccount: CanActivateFn = (route, state) => {
    const router = inject(Router);
    const user = localStorage.getItem('user');
    let userData;
    if(user){
        userData = JSON.parse(user);        
    }

    if (userData.role === "ROLE_ADMIN" || userData.appStatus === "ACTIVE") {
        return true;
    } else {
        router.navigate(['/auth']);
        return false;
    }
};
