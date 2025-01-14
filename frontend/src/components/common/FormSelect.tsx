import clsx from 'clsx';
import { type RefAttributes } from 'react';
import ReactSelect, { type ClassNamesConfig, type GroupBase } from 'react-select';
import { type SelectInstance } from 'react-select';
import { type Props } from 'react-select';
import ReactCreatableSelect, { type CreatableProps } from 'react-select/creatable';

const classNames: ClassNamesConfig<any, boolean, any> = {
    control: (state) => state.isFocused ? 'react-select-control--focused' : 'react-select-control',
    option: (state) => clsx(
        'react-select-option',
        state.isFocused && 'focused',
        state.isSelected && 'selected',
        state.isDisabled && 'disabled',
    ),
    menu: () => 'react-select-menu',
    valueContainer: () => 'react-select-value',
    dropdownIndicator: () => 'dropdown-indicator',
    indicatorSeparator: () => 'indicator-separator',
    singleValue: () => 'single-value',
};

type ValueType = string | number;


export function FormSelect<
    Option extends { value: ValueType },
    IsMulti extends boolean = false,
    Group extends GroupBase<Option> = GroupBase<Option>,
>(
    props: Omit<Props<Option, IsMulti, Group> & RefAttributes<SelectInstance<Option, IsMulti, Group>>, 'options'> & {
        options?: Option[];
    },
) {
    return (
        <ReactSelect
            {...props}
            classNames={classNames}
        />
    );
}

export function CreatableSelect<
    Option extends { value: ValueType },
    IsMulti extends boolean = false,
    Group extends GroupBase<Option> = GroupBase<Option>,
>(
    props: Omit<CreatableProps<Option, IsMulti, Group> & RefAttributes<SelectInstance<Option, IsMulti, Group>>, 'options'> & {
        options?: Option[];
    },
) {
    return (
        <ReactCreatableSelect
            {...props}
            classNames={classNames}
        />
    );
}