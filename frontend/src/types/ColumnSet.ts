/** base64 */
export type ColumnSetResponse = string;

/**
 * Represents a non-empty set of columns.
 */
export class ColumnSet {
    private constructor(
        /** Ordered indexes of the columns (ascending). */
        readonly columns: number[],
        /** Just an optimization. */
        private readonly bytes: Uint8Array,
        private base64: string,
    ) {}

    static fromResponse(input: ColumnSetResponse): ColumnSet {
        // NICE_TO_HAVE use this once fully supported:
        // Uint8Array.fromBase64
        const bytes = Uint8Array.from(atob(input), c => c.charCodeAt(0));
        const columns: number[] = [];

        for (let i = 0; i < bytes.length; i++) {
            for (let j = 0; j < 8; j++) {
                if (bytes[i] & (1 << j))
                    columns.push(i * 8 + j);
            }
        }

        return new ColumnSet(columns, bytes, input);
    }

    toResponse(): ColumnSetResponse {
        return this.base64;
    }

    static fromIndexes(indexes: number[]): ColumnSet {
        const maxIndex = Math.max(...indexes);
        const bytesLength = Math.ceil((maxIndex + 1) / 8);
        const bytes = new Uint8Array(bytesLength);

        for (const index of indexes) {
            const j = index % 8;
            const i = (index - j) / 8;

            bytes[i] |= (1 << j);
        }

        const sorted = indexes.toSorted((a, b) => a - b);
        const base64 = btoa(String.fromCharCode(...bytes));

        return new ColumnSet(sorted, bytes, base64);
    }

    has(index: number): boolean {
        const j = index % 8;
        const i = (index - j) / 8;

        return i < this.bytes.length
            && (this.bytes[i] & (1 << j)) !== 0;
    }

    map(columns: string[]): string[] {
        return this.columns.map(i => columns[i]);
    }

    toString(columnValues?: string[]): string {
        return (columnValues ? this.map(columnValues) : this.columns).join(',');
    }

    get id(): string {
        return this.base64;
    }

    get length(): number {
        return this.columns.length;
    }
}
