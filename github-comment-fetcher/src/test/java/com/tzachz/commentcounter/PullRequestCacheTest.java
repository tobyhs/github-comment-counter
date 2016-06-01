package com.tzachz.commentcounter;

import com.tzachz.commentcounter.apifacade.GitHubApiFacade;
import com.tzachz.commentcounter.apifacade.jsonobjects.GHPullRequest;
import com.tzachz.commentcounter.apifacade.jsonobjects.GHUser;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created with IntelliJ IDEA.
 * User: tzachz
 * Date: 17/08/13
 * Time: 10:34
 */
public class PullRequestCacheTest {

    @Mock
    private GitHubApiFacade facade;

    @InjectMocks
    private PullRequestCache cache;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void getReturnsFacadeResult() throws Exception {
        GHPullRequest expected = new GHPullRequest(new GHUser("user1", ""));
        when(facade.getPullRequest("url")).thenReturn(expected);
        GHPullRequest result = cache.get("url");
        assertThat(result, equalTo(expected));
    }

    @Test
    public void successfulFetchOccursOnce() throws Exception {
        when(facade.getPullRequest("url")).thenReturn(new GHPullRequest(new GHUser("user1", "")));
        verifyGetPullRequestCalledOnce();
    }

    @Test
    public void notFoundReturnsNull() {
        when(facade.getPullRequest("url")).thenReturn(null);
        assertThat(cache.get("url"), is(nullValue()));
    }

    @Test
    public void notFoundFetchesOnce() {
        when(facade.getPullRequest("url")).thenReturn(null);
        verifyGetPullRequestCalledOnce();
    }

    @Test
    public void failedFetchRetriesEveryTime() throws Exception {
        when(facade.getPullRequest("url")).thenThrow(new RuntimeException("can't access url"));
        cache.get("url");
        cache.get("url");
        verify(facade, times(2)).getPullRequest("url");
    }

    @Test
    public void failedFetchReturnsUnknownUserPullRequest() throws Exception {
        when(facade.getPullRequest("url")).thenThrow(new RuntimeException("can't access url"));
        GHPullRequest result = cache.get("url");
        assertThat(result, equalTo(PullRequestCache.UNKNOWN));
    }

    private void verifyGetPullRequestCalledOnce() {
        cache.get("url");
        cache.get("url");
        verify(facade, times(1)).getPullRequest("url");
        verifyNoMoreInteractions(facade);
    }
}
