const fs = require('fs');

function replaceInFile(file, replacer) {
  let content = fs.readFileSync(file, 'utf8');
  let newContent = replacer(content);
  if(content !== newContent) {
    fs.writeFileSync(file, newContent);
    console.log('Updated ' + file);
  }
}

// 1. Guards
const guardFiles = ['auth.guard.ts', 'admin.guard.ts', 'closedAccount.guard.ts', 'redirectIfLoggedIn.guard.ts'].map(f => 'src/app/guards/' + f);
guardFiles.forEach(file => {
  replaceInFile(file, content => {
    if(!content.includes('AuthService')) {
      content = content.replace(/(import.*?;)/, "$1\nimport { AuthService } from '../auth/auth.service';");
    }
    content = content.replace(/const user = sessionStorage\.getItem\('user'\);/, "const authService = inject(AuthService);\n  const user = authService.getCurrentUser();");
    content = content.replace(/if \(user\)/g, "if (user)");
    content = content.replace(/JSON\.parse\(user\)/g, "user");
    return content;
  });
});

// 2. Profile
replaceInFile('src/app/profile/profile.component.ts', content => {
  if(!content.includes('AuthService')) {
    content = content.replace(/(import.*?;)/, "$1\nimport { AuthService } from '../auth/auth.service';");
  }
  content = content.replace(/constructor\(\) \{/, "constructor(private authService: AuthService) {");
  content = content.replace(/const userData = sessionStorage\.getItem\('user'\);\s*this\.user = userData \? JSON\.parse\(userData\) : \{\}/, "this.user = this.authService.getCurrentUser() || {}");
  return content;
});

// 3. Navbar
replaceInFile('src/app/navbar/navbar.component.ts', content => {
  if(!content.includes('AuthService')) {
    content = content.replace(/(import.*?;)/, "$1\nimport { AuthService } from '../auth/auth.service';");
  }
  content = content.replace(/constructor\(private postService: PostService, private router: Router\)/, "constructor(private postService: PostService, private router: Router, private authService: AuthService)");
  content = content.replace(/const userData = sessionStorage\.getItem\('user'\);\s*if \(userData\) \{\s*this\.user = JSON\.parse\(userData\);\s*\}/g, "this.user = this.authService.getCurrentUser();");
  return content;
});

// 4. Add Bank
replaceInFile('src/app/add-bank/add-bank.component.ts', content => {
  if(!content.includes('AuthService')) {
    content = content.replace(/(import.*?;)/, "$1\nimport { AuthService } from '../auth/auth.service';");
  }
  content = content.replace(/private router: Router\s*\) \{/, "private router: Router,\n    private authService: AuthService\n  ) {");
  content = content.replace(/const userData = sessionStorage\.getItem\('user'\);\s*if \(userData\) \{\s*this\.user = JSON\.parse\(userData\);\s*\}/, "this.user = this.authService.getCurrentUser();");
  return content;
});

// 5. Send Money
replaceInFile('src/app/send-money/send-money.component.ts', content => {
  if(!content.includes('AuthService')) {
    content = content.replace(/(import.*?;)/, "$1\nimport { AuthService } from '../auth/auth.service';");
  }
  content = content.replace(/private router: Router\s*\) \{/, "private router: Router,\n    private authService: AuthService\n  ) {");
  content = content.replace(/const userStr = sessionStorage\.getItem\('user'\);\s*if \(userStr\) \{\s*try \{\s*const user = JSON\.parse\(userStr\);\s*this\.currentUserId = user\.vpa\.vpaId;\s*\} catch \(e\) \{\s*console\.error\('Error parsing user from sessionStorage:', e\);\s*\}\s*\}/, "const user = this.authService.getCurrentUser();\n    if (user && user.vpa) {\n      this.currentUserId = user.vpa.vpaId;\n    }");
  return content;
});

// 6. Check Balance
replaceInFile('src/app/check-balance/check-balance.component.ts', content => {
  if(!content.includes('AuthService')) {
    content = content.replace(/(import.*?;)/, "$1\nimport { AuthService } from '../auth/auth.service';");
  }
  content = content.replace(/constructor\(private postService: PostService\) \{/, "constructor(private postService: PostService, private authService: AuthService) {");
  content = content.replace(/const userData = sessionStorage\.getItem\('user'\);\s*if \(userData\) \{\s*this\.user = JSON\.parse\(userData\);\s*\}/, "this.user = this.authService.getCurrentUser();");
  return content;
});
