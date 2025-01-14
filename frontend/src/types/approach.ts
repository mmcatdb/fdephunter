export type ApproachFromServer = {
    name: string;
    author: string;
};

export class Approach {
    private constructor(
        readonly name: string,
        readonly author: string,
    ) {}

    static fromServer(input: ApproachFromServer): Approach {
        return new Approach(
            input.name,
            input.author,
        );
    }
}