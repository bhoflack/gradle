/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.internal.changedetection.state;

import org.gradle.cache.PersistentIndexedCache;
import org.gradle.internal.id.IdGenerator;
import org.gradle.internal.id.RandomLongIdGenerator;
import org.gradle.messaging.serialize.DataStreamBackedSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class CacheBackedFileSnapshotRepository implements FileSnapshotRepository {
    private final PersistentIndexedCache<Long, FileCollectionSnapshot> cache;
    private IdGenerator<Long> generator = new RandomLongIdGenerator();

    public CacheBackedFileSnapshotRepository(TaskArtifactStateCacheAccess cacheAccess) {
        cache = cacheAccess.createCache("fileSnapshots", Long.class, FileCollectionSnapshot.class, new FileSnapshotSerializer());
    }

    public Long add(FileCollectionSnapshot snapshot) {
        Long id = generator.generateId();
        cache.put(id, snapshot);
        return id;
    }

    public FileCollectionSnapshot get(Long id) {
        return cache.get(id);
    }

    public void remove(Long id) {
        cache.remove(id);
    }

    static class FileSnapshotSerializer extends DataStreamBackedSerializer<FileCollectionSnapshot> {
        @Override
        public FileCollectionSnapshot read(DataInput dataInput) throws Exception {
            int kind = dataInput.readInt();
            if (kind == 1) {
                DefaultFileSnapshotter.Serializer serializer = new DefaultFileSnapshotter.Serializer();
                return serializer.read(dataInput);
            } else if (kind == 2) {
                OutputFilesSnapshotter.Serializer serializer = new OutputFilesSnapshotter.Serializer();
                return serializer.read(dataInput);
            } else {
                throw new RuntimeException("Unable to rad from file snapshot cache. Unexpected value read.");
            }
        }

        @Override
        public void write(DataOutput dataOutput, FileCollectionSnapshot value) throws IOException {
            if (value instanceof DefaultFileSnapshotter.FileCollectionSnapshotImpl) {
                dataOutput.writeInt(1);
                DefaultFileSnapshotter.FileCollectionSnapshotImpl cached = (DefaultFileSnapshotter.FileCollectionSnapshotImpl) value;
                DefaultFileSnapshotter.Serializer serializer = new DefaultFileSnapshotter.Serializer();
                serializer.write(dataOutput, cached);
            } else if (value instanceof OutputFilesSnapshotter.OutputFilesSnapshot) {
                dataOutput.writeInt(2);
                OutputFilesSnapshotter.OutputFilesSnapshot cached = (OutputFilesSnapshotter.OutputFilesSnapshot) value;
                OutputFilesSnapshotter.Serializer serializer = new OutputFilesSnapshotter.Serializer();
                serializer.write(dataOutput, cached);
            } else {
                throw new RuntimeException("Unable to write to file snapshot cache. Unexpected type to write: " + value);
            }
        }
    }
}
