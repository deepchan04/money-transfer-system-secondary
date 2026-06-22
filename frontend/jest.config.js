module.exports = {
  preset: 'jest-preset-angular',
  // 👇 Updated path to point inside <rootDir>/src/
  setupFilesAfterEnv: ['<rootDir>/src/setup-jest.ts'], 
  testPathIgnorePatterns: [
    '<rootDir>/node_modules/',
    '<rootDir>/dist/',
  ],
};