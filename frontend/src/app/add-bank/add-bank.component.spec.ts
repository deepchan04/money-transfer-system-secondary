import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { AddBankComponent } from './add-bank.component';
import { PostService } from '../service/post.service';
import { AuthService } from '../auth/auth.service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

describe('AddBankComponent', () => {
  let component: AddBankComponent;

  const postServiceMock = {
    linkBankAccount: jest.fn()
  };

  const authServiceMock = {
    getCurrentUser: jest.fn().mockReturnValue({
      vpa: { vpaId: 'VPA123' }
    }),
    setCurrentUser: jest.fn()
  };

  const routerMock = {
    navigate: jest.fn()
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddBankComponent],
      providers: [
        { provide: PostService, useValue: postServiceMock },
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerMock }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(AddBankComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should auto-fill vpaId on init', () => {
    expect(component.vpaId).toBe('VPA123');
  });

  it('should invalidate wrong account number', () => {
    component.accountNumber = 'ABC123';
    expect(component.accountNumberInvalid).toBe(true);
  });

  it('should validate correct account number', () => {
    component.accountNumber = 'ACC12345';
    expect(component.accountNumberInvalid).toBe(false);
  });

  it('should invalidate short password', () => {
    component.accountPassword = '123';
    expect(component.accountPasswordInvalid).toBe(true);
  });

  it('should block linkBank when fields are invalid', () => {
    component.accountNumber = '';
    component.accountPassword = '';

    component.linkBank();

    expect(component.errorMessage).toBe('Please enter account number and password');
  });

  it('should call API and start countdown on success', fakeAsync(() => {
    postServiceMock.linkBankAccount.mockReturnValue(of({ success: true }));

    component.accountNumber = 'ACC123';
    component.accountPassword = '123456';
    component.vpaId = 'VPA123';

    component.linkBank();

    tick(1000); // for setTimeout
    tick();     // observable

    expect(postServiceMock.linkBankAccount).toHaveBeenCalled();
    expect(component.isLoading).toBe(false);
    expect(component.success).toBe(true);
  }));

  it('should handle API error (server down)', fakeAsync(() => {
    postServiceMock.linkBankAccount.mockReturnValue(
      throwError(() => ({ status: 0 }))
    );

    component.accountNumber = 'ACC123';
    component.accountPassword = '123456';
    component.vpaId = 'VPA123';

    component.linkBank();

    tick(1000);
    tick();

    expect(component.errorMessage).toBe('Server is currently down. Please try later.');
    expect(component.success).toBe(false);
  }));

  it('should navigate after countdown completes', fakeAsync(() => {
    postServiceMock.linkBankAccount.mockReturnValue(of({}));

    component.accountNumber = 'ACC123';
    component.accountPassword = '123456';
    component.vpaId = 'VPA123';

    component.linkBank();

    tick(1000);
    tick();

    tick(5000); // countdown

    expect(routerMock.navigate).toHaveBeenCalledWith(['/dashboard']);
  }));
});
