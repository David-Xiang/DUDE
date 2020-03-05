/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*!
 * \file tvm_runtime.h
 * \brief Pack all tvm runtime source files
 */
#include <sys/stat.h>
#include <fstream>

#include "tvm/src/runtime/c_runtime_api.cc"
#include "tvm/src/runtime/cpu_device_api.cc"
#include "tvm/src/runtime/workspace_pool.cc"
#include "tvm/src/runtime/module_util.cc"
#include "tvm/src/runtime/system_lib_module.cc"
#include "tvm/src/runtime/module.cc"
#include "tvm/src/runtime/registry.cc"
#include "tvm/src/runtime/file_util.cc"
#include "tvm/src/runtime/dso_module.cc"
#include "tvm/src/runtime/thread_pool.cc"
#include "tvm/src/runtime/object.cc"
#include "tvm/src/runtime/threading_backend.cc"
#include "tvm/src/runtime/ndarray.cc"

#include "tvm/src/runtime/graph/graph_runtime.cc"

#ifdef TVM_OPENCL_RUNTIME
#include "tvm/src/runtime/opencl/opencl_device_api.cc"
#include "tvm/src/runtime/opencl/opencl_module.cc"
#endif
