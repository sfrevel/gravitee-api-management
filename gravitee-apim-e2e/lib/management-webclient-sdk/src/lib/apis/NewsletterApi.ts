/* tslint:disable */
/* eslint-disable */
/**
 * Gravitee.io - Management API
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


import * as runtime from '../runtime';
import {
    UserEntity,
    UserEntityFromJSON,
    UserEntityToJSON,
} from '../models';

export interface GetTaglinesRequest {
    orgId: string;
}

export interface GetTaglines1Request {
    envId: string;
    orgId: string;
}

export interface SubscribeNewsletterToCurrentUserRequest {
    orgId: string;
    body: string;
}

export interface SubscribeNewsletterToCurrentUser1Request {
    envId: string;
    orgId: string;
    body: string;
}

/**
 * 
 */
export class NewsletterApi extends runtime.BaseAPI {

    /**
     * Get taglines to display in the newsletter
     */
    async getTaglinesRaw(requestParameters: GetTaglinesRequest): Promise<runtime.ApiResponse<Array<string>>> {
        if (requestParameters.orgId === null || requestParameters.orgId === undefined) {
            throw new runtime.RequiredError('orgId','Required parameter requestParameters.orgId was null or undefined when calling getTaglines.');
        }

        const queryParameters: runtime.HTTPQuery = {};

        const headerParameters: runtime.HTTPHeaders = {};

        if (this.configuration && (this.configuration.username !== undefined || this.configuration.password !== undefined)) {
            headerParameters["Authorization"] = "Basic " + btoa(this.configuration.username + ":" + this.configuration.password);
        }
        const response = await this.request({
            path: `/organizations/{orgId}/user/newsletter/taglines`.replace(`{${"orgId"}}`, encodeURIComponent(String(requestParameters.orgId))),
            method: 'GET',
            headers: headerParameters,
            query: queryParameters,
        });

        return new runtime.JSONApiResponse<any>(response);
    }

    /**
     * Get taglines to display in the newsletter
     */
    async getTaglines(requestParameters: GetTaglinesRequest): Promise<Array<string>> {
        const response = await this.getTaglinesRaw(requestParameters);
        return await response.value();
    }

    /**
     * Get taglines to display in the newsletter
     */
    async getTaglines1Raw(requestParameters: GetTaglines1Request): Promise<runtime.ApiResponse<Array<string>>> {
        if (requestParameters.envId === null || requestParameters.envId === undefined) {
            throw new runtime.RequiredError('envId','Required parameter requestParameters.envId was null or undefined when calling getTaglines1.');
        }

        if (requestParameters.orgId === null || requestParameters.orgId === undefined) {
            throw new runtime.RequiredError('orgId','Required parameter requestParameters.orgId was null or undefined when calling getTaglines1.');
        }

        const queryParameters: runtime.HTTPQuery = {};

        const headerParameters: runtime.HTTPHeaders = {};

        if (this.configuration && (this.configuration.username !== undefined || this.configuration.password !== undefined)) {
            headerParameters["Authorization"] = "Basic " + btoa(this.configuration.username + ":" + this.configuration.password);
        }
        const response = await this.request({
            path: `/organizations/{orgId}/environments/{envId}/user/newsletter/taglines`.replace(`{${"envId"}}`, encodeURIComponent(String(requestParameters.envId))).replace(`{${"orgId"}}`, encodeURIComponent(String(requestParameters.orgId))),
            method: 'GET',
            headers: headerParameters,
            query: queryParameters,
        });

        return new runtime.JSONApiResponse<any>(response);
    }

    /**
     * Get taglines to display in the newsletter
     */
    async getTaglines1(requestParameters: GetTaglines1Request): Promise<Array<string>> {
        const response = await this.getTaglines1Raw(requestParameters);
        return await response.value();
    }

    /**
     * Subscribe to the newsletter the authenticated user
     */
    async subscribeNewsletterToCurrentUserRaw(requestParameters: SubscribeNewsletterToCurrentUserRequest): Promise<runtime.ApiResponse<UserEntity>> {
        if (requestParameters.orgId === null || requestParameters.orgId === undefined) {
            throw new runtime.RequiredError('orgId','Required parameter requestParameters.orgId was null or undefined when calling subscribeNewsletterToCurrentUser.');
        }

        if (requestParameters.body === null || requestParameters.body === undefined) {
            throw new runtime.RequiredError('body','Required parameter requestParameters.body was null or undefined when calling subscribeNewsletterToCurrentUser.');
        }

        const queryParameters: runtime.HTTPQuery = {};

        const headerParameters: runtime.HTTPHeaders = {};

        headerParameters['Content-Type'] = 'application/json';

        if (this.configuration && (this.configuration.username !== undefined || this.configuration.password !== undefined)) {
            headerParameters["Authorization"] = "Basic " + btoa(this.configuration.username + ":" + this.configuration.password);
        }
        const response = await this.request({
            path: `/organizations/{orgId}/user/newsletter/_subscribe`.replace(`{${"orgId"}}`, encodeURIComponent(String(requestParameters.orgId))),
            method: 'POST',
            headers: headerParameters,
            query: queryParameters,
            body: requestParameters.body as any,
        });

        return new runtime.JSONApiResponse(response, (jsonValue) => UserEntityFromJSON(jsonValue));
    }

    /**
     * Subscribe to the newsletter the authenticated user
     */
    async subscribeNewsletterToCurrentUser(requestParameters: SubscribeNewsletterToCurrentUserRequest): Promise<UserEntity> {
        const response = await this.subscribeNewsletterToCurrentUserRaw(requestParameters);
        return await response.value();
    }

    /**
     * Subscribe to the newsletter the authenticated user
     */
    async subscribeNewsletterToCurrentUser1Raw(requestParameters: SubscribeNewsletterToCurrentUser1Request): Promise<runtime.ApiResponse<UserEntity>> {
        if (requestParameters.envId === null || requestParameters.envId === undefined) {
            throw new runtime.RequiredError('envId','Required parameter requestParameters.envId was null or undefined when calling subscribeNewsletterToCurrentUser1.');
        }

        if (requestParameters.orgId === null || requestParameters.orgId === undefined) {
            throw new runtime.RequiredError('orgId','Required parameter requestParameters.orgId was null or undefined when calling subscribeNewsletterToCurrentUser1.');
        }

        if (requestParameters.body === null || requestParameters.body === undefined) {
            throw new runtime.RequiredError('body','Required parameter requestParameters.body was null or undefined when calling subscribeNewsletterToCurrentUser1.');
        }

        const queryParameters: runtime.HTTPQuery = {};

        const headerParameters: runtime.HTTPHeaders = {};

        headerParameters['Content-Type'] = 'application/json';

        if (this.configuration && (this.configuration.username !== undefined || this.configuration.password !== undefined)) {
            headerParameters["Authorization"] = "Basic " + btoa(this.configuration.username + ":" + this.configuration.password);
        }
        const response = await this.request({
            path: `/organizations/{orgId}/environments/{envId}/user/newsletter/_subscribe`.replace(`{${"envId"}}`, encodeURIComponent(String(requestParameters.envId))).replace(`{${"orgId"}}`, encodeURIComponent(String(requestParameters.orgId))),
            method: 'POST',
            headers: headerParameters,
            query: queryParameters,
            body: requestParameters.body as any,
        });

        return new runtime.JSONApiResponse(response, (jsonValue) => UserEntityFromJSON(jsonValue));
    }

    /**
     * Subscribe to the newsletter the authenticated user
     */
    async subscribeNewsletterToCurrentUser1(requestParameters: SubscribeNewsletterToCurrentUser1Request): Promise<UserEntity> {
        const response = await this.subscribeNewsletterToCurrentUser1Raw(requestParameters);
        return await response.value();
    }

}