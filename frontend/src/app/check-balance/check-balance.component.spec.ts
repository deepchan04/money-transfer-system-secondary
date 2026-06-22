import { TestBed } from '@angular/core/testing';
import { CheckBalanceComponent } from './check-balance.component';
import { PostService } from '../service/post.service';
import { AuthService } from '../auth/auth.service';
import { of, throwError } from 'rxjs';

describe('CheckBalanceComponent', () => {
  let component: CheckBalanceComponent;

  const postServiceMock = {
    getBalance: jest.fn()
  };

  const authServiceMock = {
    getCurrentUser: jest.fn().mockReturnValue({
      vpa: { vpaId: 'VPA123' }
    })
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CheckBalanceComponent],
      providers: [
        { provide: PostService, useValue: postServiceMock },
        { provide: AuthService, useValue: authServiceMock }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(CheckBalanceComponent);
    component = fixture.componentInstance;
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should load user from AuthService', () => {
    expect(component.user.vpa.vpaId).toBe('VPA123');
  });

  it('should not call API if password is empty', () => {
    component.password = '';

    component.fetchBalance();

    expect(postServiceMock.getBalance).not.toHaveBeenCalled();
  });

  it('should not call API if password is too short', () => {
    component.password = '123';

    component.fetchBalance();

    expect(postServiceMock.getBalance).not.toHaveBeenCalled();
  });

  it('should call API and set balance on success', () => {
    postServiceMock.getBalance.mockReturnValue(
      of({ balance: 5000 })
    );

    component.password = '123456';

    component.fetchBalance();

    expect(postServiceMock.getBalance).toHaveBeenCalledWith(
      'VPA123',
      '123456'
    );

    expect(component.balance).toBe(5000);
  });

  it('should handle API error - server down', () => {
    postServiceMock.getBalance.mockReturnValue(
      throwError(() => ({ status: 0 }))
    );

    component.password = '123456';

    component.fetchBalance();

    expect(component.errorMessage).toBe(
      'Server is currently down. Please try later.'
    );
  });

  it('should handle API error - invalid credentials', () => {
    postServiceMock.getBalance.mockReturnValue(
      throwError(() => ({
        status: 400,
        error: { message: 'Invalid password' }
      }))
    );

    component.password = '123456';

    component.fetchBalance();

    expect(component.errorMessage).toBe('Invalid password');
  });

  it('should set balance from direct number response', () => {
    postServiceMock.getBalance.mockReturnValue(
      of(10000) // backend returns number directly
    );

    component.password = '123456';

    component.fetchBalance();

    expect(component.balance).toBe(10000);
  });
});