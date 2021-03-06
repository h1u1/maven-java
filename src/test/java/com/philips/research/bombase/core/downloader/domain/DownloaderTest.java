/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.bombase.core.downloader.domain;

import com.philips.research.bombase.core.downloader.DownloadException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DownloaderTest {
    private static final String TOOL = "tool";
    private static final URI LOCATION = URI.create("https://example.com@version");

    private static Path testDirectory;

    private final VcsHandler mockHandler = mock(VcsHandler.class);
    private final Downloader downloader = new Downloader();

    @BeforeEach()
    void beforeEach() throws IOException {
        testDirectory = Files.createTempDirectory("test");
    }

    @AfterEach
    void afterEach() throws IOException {
        FileSystemUtils.deleteRecursively(testDirectory);
    }

    @Test
    void throws_downloadForUnknownVcsTool() {
        final var unknown = URI.create("unknown+http://unknown.org");

        assertThatThrownBy(() -> downloader.download(testDirectory, unknown))
                .isInstanceOf(DownloadException.class)
                .hasMessageContaining("No handler registered");
    }

    @Test
    void downloadsForToolFromLocationToDirectory() {
        final var location = LOCATION + "#path/to/whatever";
        final var destinationDir = testDirectory.resolve("whatever");
        when(mockHandler.download(testDirectory, URI.create(location))).thenReturn(destinationDir);

        downloader.register(TOOL, mockHandler);
        final var directory = downloader.download(testDirectory, URI.create(TOOL + "+" + location));

        assertThat(directory).isEqualTo(destinationDir);
    }
}
