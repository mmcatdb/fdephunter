export type UserFromServer = {
    id: string;
    firstName: string;
    lastName: string;
    email: string;
}

export class User {
    private constructor(
        readonly id: string,
        readonly name: string,
        readonly firstName: string,
        readonly lastName: string,
        readonly email: string,
    ) {}

    static fromServer(input: UserFromServer): User {
        return new User(
            input.id,
            input.firstName + ' ' + input.lastName,
            input.firstName,
            input.lastName,
            input.email,
        );
    }
}
