/* tslint:disable */
/* eslint-disable */
/**
 * Gravitee.io - Management API
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 3.18.0-SNAPSHOT
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

import { exists, mapValues } from '../runtime';
import {
    UserRoleEntity,
    UserRoleEntityFromJSON,
    UserRoleEntityFromJSONTyped,
    UserRoleEntityToJSON,
} from './';

/**
 * 
 * @export
 * @interface UserEntity
 */
export interface UserEntity {
    /**
     * 
     * @type {string}
     * @memberof UserEntity
     */
    id?: string;
    /**
     * 
     * @type {string}
     * @memberof UserEntity
     */
    firstname?: string;
    /**
     * 
     * @type {string}
     * @memberof UserEntity
     */
    lastname?: string;
    /**
     * 
     * @type {string}
     * @memberof UserEntity
     */
    password?: string;
    /**
     * 
     * @type {string}
     * @memberof UserEntity
     */
    email?: string;
    /**
     * 
     * @type {Array<UserRoleEntity>}
     * @memberof UserEntity
     */
    roles?: Array<UserRoleEntity>;
    /**
     * 
     * @type {{ [key: string]: Array<UserRoleEntity>; }}
     * @memberof UserEntity
     */
    envRoles?: { [key: string]: Array<UserRoleEntity>; };
    /**
     * 
     * @type {string}
     * @memberof UserEntity
     */
    picture?: string;
    /**
     * 
     * @type {string}
     * @memberof UserEntity
     */
    source?: string;
    /**
     * 
     * @type {string}
     * @memberof UserEntity
     */
    sourceId?: string;
    /**
     * 
     * @type {Date}
     * @memberof UserEntity
     */
    lastConnectionAt?: Date;
    /**
     * 
     * @type {Date}
     * @memberof UserEntity
     */
    firstConnectionAt?: Date;
    /**
     * 
     * @type {string}
     * @memberof UserEntity
     */
    status?: string;
    /**
     * 
     * @type {number}
     * @memberof UserEntity
     */
    loginCount?: number;
    /**
     * 
     * @type {boolean}
     * @memberof UserEntity
     */
    newsletterSubscribed?: boolean;
    /**
     * 
     * @type {{ [key: string]: any; }}
     * @memberof UserEntity
     */
    customFields?: { [key: string]: any; };
    /**
     * 
     * @type {string}
     * @memberof UserEntity
     */
    readonly displayName?: string;
    /**
     * 
     * @type {Date}
     * @memberof UserEntity
     */
    created_at?: Date;
    /**
     * 
     * @type {Date}
     * @memberof UserEntity
     */
    updated_at?: Date;
    /**
     * 
     * @type {boolean}
     * @memberof UserEntity
     */
    primary_owner?: boolean;
    /**
     * 
     * @type {number}
     * @memberof UserEntity
     */
    number_of_active_tokens?: number;
}

export function UserEntityFromJSON(json: any): UserEntity {
    return UserEntityFromJSONTyped(json, false);
}

export function UserEntityFromJSONTyped(json: any, ignoreDiscriminator: boolean): UserEntity {
    if ((json === undefined) || (json === null)) {
        return json;
    }
    return {
        
        'id': !exists(json, 'id') ? undefined : json['id'],
        'firstname': !exists(json, 'firstname') ? undefined : json['firstname'],
        'lastname': !exists(json, 'lastname') ? undefined : json['lastname'],
        'password': !exists(json, 'password') ? undefined : json['password'],
        'email': !exists(json, 'email') ? undefined : json['email'],
        'roles': !exists(json, 'roles') ? undefined : ((json['roles'] as Array<any>).map(UserRoleEntityFromJSON)),
        'envRoles': !exists(json, 'envRoles') ? undefined : json['envRoles'],
        'picture': !exists(json, 'picture') ? undefined : json['picture'],
        'source': !exists(json, 'source') ? undefined : json['source'],
        'sourceId': !exists(json, 'sourceId') ? undefined : json['sourceId'],
        'lastConnectionAt': !exists(json, 'lastConnectionAt') ? undefined : (new Date(json['lastConnectionAt'])),
        'firstConnectionAt': !exists(json, 'firstConnectionAt') ? undefined : (new Date(json['firstConnectionAt'])),
        'status': !exists(json, 'status') ? undefined : json['status'],
        'loginCount': !exists(json, 'loginCount') ? undefined : json['loginCount'],
        'newsletterSubscribed': !exists(json, 'newsletterSubscribed') ? undefined : json['newsletterSubscribed'],
        'customFields': !exists(json, 'customFields') ? undefined : json['customFields'],
        'displayName': !exists(json, 'displayName') ? undefined : json['displayName'],
        'created_at': !exists(json, 'created_at') ? undefined : (new Date(json['created_at'])),
        'updated_at': !exists(json, 'updated_at') ? undefined : (new Date(json['updated_at'])),
        'primary_owner': !exists(json, 'primary_owner') ? undefined : json['primary_owner'],
        'number_of_active_tokens': !exists(json, 'number_of_active_tokens') ? undefined : json['number_of_active_tokens'],
    };
}

export function UserEntityToJSON(value?: UserEntity | null): any {
    if (value === undefined) {
        return undefined;
    }
    if (value === null) {
        return null;
    }
    return {
        
        'id': value.id,
        'firstname': value.firstname,
        'lastname': value.lastname,
        'password': value.password,
        'email': value.email,
        'roles': value.roles === undefined ? undefined : ((value.roles as Array<any>).map(UserRoleEntityToJSON)),
        'envRoles': value.envRoles,
        'picture': value.picture,
        'source': value.source,
        'sourceId': value.sourceId,
        'lastConnectionAt': value.lastConnectionAt === undefined ? undefined : (value.lastConnectionAt.toISOString()),
        'firstConnectionAt': value.firstConnectionAt === undefined ? undefined : (value.firstConnectionAt.toISOString()),
        'status': value.status,
        'loginCount': value.loginCount,
        'newsletterSubscribed': value.newsletterSubscribed,
        'customFields': value.customFields,
        'created_at': value.created_at === undefined ? undefined : (value.created_at.toISOString()),
        'updated_at': value.updated_at === undefined ? undefined : (value.updated_at.toISOString()),
        'primary_owner': value.primary_owner,
        'number_of_active_tokens': value.number_of_active_tokens,
    };
}

