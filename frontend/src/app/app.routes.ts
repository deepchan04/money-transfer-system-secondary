import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { AuthComponent } from './auth/auth.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ProfileComponent } from './profile/profile.component';
import { TransactionsComponent } from './transactions/transactions.component';
import { SendMoneyComponent } from './send-money/send-money.component';
import { AddBankComponent } from './add-bank/add-bank.component';
import { CheckBalanceComponent } from './check-balance/check-balance.component';
import { AdminLoginComponent } from './admin/admin-login/admin-login.component';
import { AdminDashboardComponent } from './admin/admin-dashboard/admin-dashboard.component';
import { AdminTransactionsComponent } from './admin/admin-transactions/admin-transactions.component';
import { AdminUsersComponent } from './admin/admin-users/admin-users.component';
import { authGuard } from './guards/auth.guard';
import { closedAccount } from './guards/closedAccount.guard';
import { adminGuard } from './guards/admin.guard';
import { RewardsComponent } from './rewards/rewards.component';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';

export const routes: Routes = [
  { path: 'auth', component: AuthComponent },
  { path: 'admin-login', component: AdminLoginComponent },
  {
  path: '',
  component: MainLayoutComponent,
  children: [
  { path: '', component: HomeComponent },
  { path: 'admin-dashboard', component: AdminDashboardComponent, canActivate: [authGuard, adminGuard] },
  { path: 'admin/transactions', component: AdminTransactionsComponent, canActivate: [authGuard, adminGuard] },
  { path: 'admin/users', component: AdminUsersComponent, canActivate: [authGuard, adminGuard] },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'profile', component: ProfileComponent, canActivate: [authGuard] },
  { path: 'transactions', component: TransactionsComponent, canActivate: [authGuard, closedAccount] },
  { path: 'send-money', component: SendMoneyComponent, canActivate: [authGuard, closedAccount] },
  { path: 'add-bank', component: AddBankComponent, canActivate: [authGuard, closedAccount] },
  { path: 'check-balance', component: CheckBalanceComponent, canActivate: [authGuard, closedAccount] },
  { path: 'rewards', component: RewardsComponent, canActivate: [authGuard, closedAccount] },
  { path: '**', redirectTo: '' }
]}];
