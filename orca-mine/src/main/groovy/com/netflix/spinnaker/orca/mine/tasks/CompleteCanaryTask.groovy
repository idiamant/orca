/*
 * Copyright 2015 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.orca.mine.tasks

import com.netflix.spinnaker.orca.api.pipeline.models.ExecutionStatus
import com.netflix.spinnaker.orca.api.pipeline.Task
import com.netflix.spinnaker.orca.api.pipeline.models.StageExecution
import com.netflix.spinnaker.orca.api.pipeline.TaskResult
import org.springframework.stereotype.Component

import javax.annotation.Nonnull

@Component
class CompleteCanaryTask implements Task {
  @Nonnull
  @Override
  TaskResult execute(@Nonnull StageExecution stage) {
    Map canary = stage.context.canary
    if (canary.status?.status == 'CANCELED') {
      return TaskResult.ofStatus(ExecutionStatus.CANCELED)
    } else if (canary.canaryResult?.overallResult == 'SUCCESS') {
      return TaskResult.ofStatus(ExecutionStatus.SUCCEEDED)
    } else if (canary?.health?.health in ["UNHEALTHY", "UNKNOWN"] || canary.canaryResult?.overallResult == 'FAILURE') {
      return TaskResult.ofStatus(stage.context.continueOnUnhealthy == true
          ? ExecutionStatus.FAILED_CONTINUE
          : ExecutionStatus.TERMINAL)
    } else {
      throw new IllegalStateException("Canary in unhandled state")
    }
  }
}
