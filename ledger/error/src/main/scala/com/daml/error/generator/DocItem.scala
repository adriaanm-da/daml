// Copyright (c) 2021 Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.daml.error.generator

import com.daml.error.{Explanation, Resolution}

case class DocItem(
    className: String,
    category: String,
    hierarchicalGrouping: List[String],
    conveyance: String,
    code: String,
    explanation: Explanation,
    resolution: Resolution,
)
