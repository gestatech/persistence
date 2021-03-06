/**
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.persistence.util.common.type.page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opendaylight.persistence.util.common.Converter;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Offset based data page.
 *
 * @param <D> type of the data
 * @author Fabiel Zuniga
 * @author Nachiket Abhyankar
 */
public class OffsetPage<D> extends Page<OffsetPageRequest, D> {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final OffsetPage EMPTY_PAGE = new OffsetPage(new OffsetPageRequest(0, 1), Collections.emptyList(), 0);

    private final long totalRecordCount;

    /**
     * Creates a data page.
     *
     * @param pageRequest request that generated this page
     * @param data page's data
     * @param totalRecordCount total number of records in the data source
     */
    public OffsetPage(OffsetPageRequest pageRequest, List<D> data, long totalRecordCount) {
        super(pageRequest, data);
        Preconditions.checkArgument(totalRecordCount >= 0, "totalRecordCount must be greater or equals to zero");
        this.totalRecordCount = totalRecordCount;
    }

    /**
     * Returns the empty page (immutable).
     *
     * @return an empty page
     */
    @SuppressWarnings({"cast", "unchecked"})
    public static final <T> OffsetPage<T> emptyPage() {
        return (OffsetPage<T>)EMPTY_PAGE;
    }

    /**
     * Gets the total number of records.
     *
     * @return the total number of records
     */
    public long getTotalRecordCount() {
        return this.totalRecordCount;
    }

    /**
     * Calculates the number of pages based on the offset and limit.
     *
     * @return the total number of pages
     */
    public int getTotalPageCount() {
        long pageCount = 0;

        if (this.totalRecordCount > 0) {
            long recordsUpToThisPage = this.totalRecordCount;

            if (this.totalRecordCount >= getRequest().getOffset()) {
                recordsUpToThisPage = getRequest().getOffset() + getData().size();
            }

            long recordsAfterThisPage = this.totalRecordCount - recordsUpToThisPage;

            long pagesUpToThisPage = 0;
            if (recordsUpToThisPage > 0) {
                if (recordsUpToThisPage >= getRequest().getOffset()) {
                    pagesUpToThisPage = getRequest().getPageIndex() + 1;
                }
                else {
                    pagesUpToThisPage = recordsUpToThisPage / getRequest().getSize();

                    if (recordsUpToThisPage % getRequest().getSize() != 0) {
                        pagesUpToThisPage++;
                    }
                }
            }

            long pagesAfterThisPage = 0;
            if (recordsAfterThisPage > 0) {
                pagesAfterThisPage = recordsAfterThisPage / getRequest().getSize();

                if (recordsAfterThisPage % getRequest().getSize() != 0) {
                    pagesAfterThisPage++;
                }
            }

            pageCount = pagesUpToThisPage + pagesAfterThisPage;
        }

        return (int)pageCount;
    }

    /**
     * Converts a page to a different data type.
     *
     * @param converter converter
     * @return a page with the new data type
     */
    public <T> OffsetPage<T> convert(Converter<D, T> converter) {
        List<T> targetItems = new ArrayList<T>(getData().size());
        for (D item : getData()) {
            targetItems.add(converter.convert(item));
        }

        return new OffsetPage<T>(getRequest(), targetItems, this.totalRecordCount);
    }

    /**
     * Checks whether there is a page after this one.
     *
     * @return {@code true} if there is a page after this one, {@code false} otherwise
     */
    public boolean hasNext() {
        return getRequest().getPageIndex() < (getTotalPageCount() - 1);
    }

    /**
     * Checks whether there is a page previous to this one.
     *
     * @return {@code true} if there is a page previous to this one, {@code false} otherwise
     */
    public boolean hasPrevious() {
        return getRequest().getPageIndex() > 0;
    }

    /**
     * Creates a request for the next page.
     *
     * @return a request for the next page if there is a page, {@code null} otherwise
     */
    public OffsetPageRequest getNextPageRequest() {
        OffsetPageRequest request = null;

        if (hasNext()) {
            request = new OffsetPageRequest(getRequest().getOffset() + getData().size(),
                    getRequest().getSize());
        }

        return request;
    }

    /**
     * Creates a request for the previous page.
     *
     * @return a request for the previous page if there is a page, {@code null} otherwise
     */
    public OffsetPageRequest getPreviousPageRequest() {
        OffsetPageRequest request = null;

        if (hasPrevious()) {
            long offset = getRequest().getOffset() - getRequest().getSize();
            request = new OffsetPageRequest(offset >= 0 ? offset : 0, getRequest().getSize());
        }

        return request;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).
                add("pageRequest", getRequest()).
                add("totalRecordCount", this.totalRecordCount).
                add("data", getData()).toString();
    }
}