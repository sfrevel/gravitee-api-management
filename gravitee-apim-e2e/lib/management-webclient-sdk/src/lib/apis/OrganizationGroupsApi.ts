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
    GroupSimpleEntity,
    GroupSimpleEntityFromJSON,
    GroupSimpleEntityToJSON,
} from '../models';

export interface GetGroups1Request {
    orgId: string;
}

/**
 * 
 */
export class OrganizationGroupsApi extends runtime.BaseAPI {

    /**
     * Find all groups for current organizationOnly users with ORGANIZATION_TAG permissions could see API groups.
     * Find groups
     */
    async getGroups1Raw(requestParameters: GetGroups1Request): Promise<runtime.ApiResponse<Array<GroupSimpleEntity>>> {
        if (requestParameters.orgId === null || requestParameters.orgId === undefined) {
            throw new runtime.RequiredError('orgId','Required parameter requestParameters.orgId was null or undefined when calling getGroups1.');
        }

        const queryParameters: runtime.HTTPQuery = {};

        const headerParameters: runtime.HTTPHeaders = {};

        if (this.configuration && (this.configuration.username !== undefined || this.configuration.password !== undefined)) {
            headerParameters["Authorization"] = "Basic " + btoa(this.configuration.username + ":" + this.configuration.password);
        }
        const response = await this.request({
            path: `/organizations/{orgId}/groups`.replace(`{${"orgId"}}`, encodeURIComponent(String(requestParameters.orgId))),
            method: 'GET',
            headers: headerParameters,
            query: queryParameters,
        });

        return new runtime.JSONApiResponse(response, (jsonValue) => jsonValue.map(GroupSimpleEntityFromJSON));
    }

    /**
     * Find all groups for current organizationOnly users with ORGANIZATION_TAG permissions could see API groups.
     * Find groups
     */
    async getGroups1(requestParameters: GetGroups1Request): Promise<Array<GroupSimpleEntity>> {
        const response = await this.getGroups1Raw(requestParameters);
        return await response.value();
    }

}