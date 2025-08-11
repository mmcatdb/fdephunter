import { type ReactNode, useCallback, useRef, useState } from 'react';
import clsx from 'clsx';
import { LuPaperclip } from 'react-icons/lu';
import { MdModeEdit } from 'react-icons/md';
import { TbTrashX } from 'react-icons/tb';
import { Spinner } from '@heroui/react';
import { API } from '@/utils/api/api';
import { type FileResponse } from '@/utils/api/routes/dataset';

export type FileInputValue = FileResponse | undefined;

const ACCEPTED_MIME_TYPES = 'text/csv';

type FileInputProps = Readonly<{
    id?: string;
    value: FileInputValue;
    onChange(value: FileInputValue): void;
}>;

export function FileInput({ id, value, onChange }: FileInputProps) {
    const [ isFetching, setIsFetching ] = useState(false);

    const handleChange = useCallback(async (file: File | null) => {
        if (!file) {
            onChange(undefined);
            return;
        }

        const formData = new FormData();
        formData.append('file', file);

        setIsFetching(true);
        const response = await API.dataset.uploadFile({}, formData);
        setIsFetching(false);

        if (!response.status) {
            console.error('Failed to upload file');
            return;
        }

        onChange(response.data);

    }, [ onChange ]);

    const filename = value?.originalName;

    return (
        <RawFileInput
            id={id}
            onChange={handleChange}
            accept={ACCEPTED_MIME_TYPES}
            preview={filename ? <FilePreview filename={filename} /> : undefined}
            isFetching={isFetching}
        />
    );
}

function FilePreview({ filename }: { filename: string }) {
    return (<>
        <div className='shrink-0'>
            <LuPaperclip size={18} />
        </div>

        <div className='grow min-w-0 break-words'>
            {filename}
        </div>
    </>);
}

type RawFileInputProps = Readonly<{
    id?: string;
    onChange: (fileList: File | null) => void;
    /** Comma-separated MIME types. Will be checked by the html input. */
    accept?: string;
    /** If present, will the input is considered full. */
    preview: ReactNode | undefined;
    isFetching?: boolean;
}>;

function RawFileInput({ id, onChange, accept, preview, isFetching }: RawFileInputProps) {
    const inputRef = useRef<HTMLInputElement>(null);

    function handleInput(fileList: FileList | null) {
        if (!fileList || fileList.length === 0)
            return;

        const file = fileList[0];
        if (accept && !accept.includes(file.type)) {
            console.error(`File type ${file.type} is not allowed. Expected: ${accept}`);
            return;
        }

        onChange(fileList[0]);
    }

    if (isFetching) {
        return (
            <div className='w-full'>
                <div className='h-14 p-3 flex items-center justify-center gap-2 rounded-lg border leading-5 bg-default-100'>
                    <Spinner size='sm' color='white' />
                    Uploading...
                </div>
            </div>
        );
    }

    return (
        <div className='w-full'>
            <input
                type='file'
                id={id}
                onChange={e => {
                    const files = e.target.files;
                    handleInput(files);
                    // This is necessary to allow the same file to be uploaded again (so that the user can choose a different crop for example).
                    e.target.value = '';
                }}
                ref={inputRef}
                accept={accept}
                className='hidden'
                aria-hidden
                tabIndex={-1}
            />
            <div
                className={clsx(
                    'group h-14 p-3 flex items-center justify-center rounded-lg border leading-5 bg-default-100',
                    preview ? 'gap-2 cursor-auto' : 'border-dashed hover:bg-default-200',
                )}
                role='button'
                aria-label='Upload file by dragging and dropping'
                tabIndex={0}
                onKeyDown={e => {
                    if (e.key === 'Enter' || e.key === ' ')
                        inputRef.current?.click();
                }}
                onDragOver={e => {
                    e.preventDefault();
                    e.dataTransfer.dropEffect = 'copy';
                }}
                onDrop={e => {
                    e.preventDefault();
                    handleInput(e.dataTransfer.files);
                }}
                onClick={() => {
                    if (!preview)
                        inputRef.current?.click();
                }}
            >
                {preview ? (<>
                    {preview}

                    <button
                        type='button'
                        onClick={() => inputRef.current?.click()}
                        aria-label='Edit uploaded file'
                        className='hover:text-primary'
                    >
                        <MdModeEdit size={18} />
                    </button>

                    <button
                        type='button'
                        onClick={() => onChange(null)}
                        aria-label='Remove uploaded file'
                        className='hover:text-danger-500'
                    >
                        <TbTrashX size={18} />
                    </button>
                </>) : (<>
                    <button
                        type='button'
                        onClick={e => {
                            e.stopPropagation();
                            inputRef.current?.click();
                        }}
                        aria-label='Upload file'
                        className='flex items-center gap-2 text-nowrap group-hover:text-primary'
                    >
                        <LuPaperclip size={18} />
                        Click to upload
                    </button>
                    {/* If you are on the phone, you very much can't use drag and drop. */}
                    <span className='ps-1 max-sm:hidden'>or drag & drop</span>
                </>)}
            </div>
        </div>
    );
}
