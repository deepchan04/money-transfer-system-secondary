export interface User {
    id: number;
    name: string;
    email: string;
    password?: string; // Optional as we might not always want to expose it
    phoneNumber: string;
    role: string;
    bankAccount: any; // Define a more specific type if known, e.g., BankAccount
    vpa: {
        id: number;
        vpaId: string;
    };
}
