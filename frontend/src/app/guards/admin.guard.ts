import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';

export const adminGuard: CanActivateFn = (route, state) => {
    const router = inject(Router);
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');
    let userData;
    if(user){
        userData = JSON.parse(user);        
    }

    if (userData.role === "ROLE_ADMIN") {
        return true;
    } else {
        router.navigate(['/auth']);
        return false;
    }
};
