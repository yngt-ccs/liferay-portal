/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.servlet.filters.util;

import com.liferay.portal.kernel.cache.key.CacheKeyGenerator;
import com.liferay.portal.kernel.cache.key.CacheKeyGeneratorUtil;
import com.liferay.portal.kernel.util.Digester;
import com.liferay.portal.kernel.util.DigesterUtil;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.InstancePool;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.util.PropsUtil;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Miguel Pastor
 * @author Carlos Sierra Andrés
 */
public class CacheFileNameGenerator {

	public static String getCacheFileName(
		HttpServletRequest request, String cacheName) {

		CacheKeyGenerator cacheKeyGenerator =
			CacheKeyGeneratorUtil.getCacheKeyGenerator(cacheName);

		cacheKeyGenerator.append(HttpUtil.getProtocol(request.isSecure()));
		cacheKeyGenerator.append(StringPool.UNDERLINE);
		cacheKeyGenerator.append(request.getRequestURI());

		StringBundler queryStringSB = new StringBundler(
			_cacheFileNameContributors.size() * 4);

		for (
			CacheFileNameContributor cacheFileNameContributor :
				_cacheFileNameContributors) {

			String value = cacheFileNameContributor.getParameterValue(request);

			if (value == null) {
				continue;
			}

			queryStringSB.append(StringPool.UNDERLINE);
			queryStringSB.append(cacheFileNameContributor.getParameterName());
			queryStringSB.append(StringPool.UNDERLINE);
			queryStringSB.append(value);
		}

		cacheKeyGenerator.append(
			DigesterUtil.digestBase64(
				Digester.SHA_256, queryStringSB.toString()));

		return _sterilizeFileName(String.valueOf(cacheKeyGenerator.finish()));
	}

	private static String _sterilizeFileName(String fileName) {
		return StringUtil.replace(
			fileName, new String[] {StringPool.SLASH, StringPool.BACK_SLASH},
			new String[] {StringPool.UNDERLINE, StringPool.UNDERLINE});
	}

	private static List<CacheFileNameContributor> _cacheFileNameContributors;

	static {
		String[] cacheFileNameContributors = PropsUtil.getArray(
			PropsKeys.CACHE_FILE_NAME_CONTRIBUTORS);

		_cacheFileNameContributors = new ArrayList<CacheFileNameContributor>(
			cacheFileNameContributors.length);

		for (String cacheFileNameContributor : cacheFileNameContributors) {
			CacheFileNameContributor cacheFileNameContributorInstance =
				(CacheFileNameContributor)InstancePool.get(
					cacheFileNameContributor);

			_cacheFileNameContributors.add(cacheFileNameContributorInstance);
		}
	}

}