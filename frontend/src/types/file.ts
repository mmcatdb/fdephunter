import { type Id } from './id';

export type FileResponse = {
    id: Id;
    originalName: string;
    hashName: string;
    size: number;
};
