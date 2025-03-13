type EnumObject<K = string | number> = Record<string, K>;
type Enum<K, E extends EnumObject<K>> = E extends Record<string, infer T> ? T : never;

export function getStringEnumValues<E extends EnumObject<string>>(enumObject: E): Enum<string, E>[] {
    return Object.keys(enumObject).map(key => enumObject[key] as Enum<string, E>);
}

const percentFormatter = Intl.NumberFormat(undefined, {
    style: 'percent',
});

export function displayPercent(value: number) {
    return percentFormatter.format(value);
}

/** Comparison of only ascii-like strings. */
export function compareStringsAscii(a: string, b: string): number {
    return a < b ? -1 : (a > b ? 1 : 0);
}
