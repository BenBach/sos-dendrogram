/*
 * Copyright 2004-2010 Information & Software Engineering Group (188/1)
 *                     Institute of Software Technology and Interactive Systems
 *                     Vienna University of Technology, Austria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.ifs.tuwien.ac.at/dm/somtoolbox/license.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/** 
 Provides the actual implementations of network models. All new models are supposed to implement {@link at.tuwien.ifs.somtoolbox.models.NetworkModel}. Basic support is
 provided by common methods in {@link at.tuwien.ifs.somtoolbox.models.AbstractNetworkModel}.
 <p>Currentely, the SOMToolbox provides the following models:</p>
 <ul>
 <li>Growing SOM</li>
 <li>Growing-Hierarchical Self-Organising Map</li>
 <li>Mnemonic SOM</li>
 </ul>

 @see at.tuwien.ifs.somtoolbox.models.GrowingSOM
 @see at.tuwien.ifs.somtoolbox.models.GHSOM
 @see at.tuwien.ifs.somtoolbox.models.MnemonicSOM
 @see at.tuwien.ifs.somtoolbox.models.AbstractNetworkModel
 */
package at.tuwien.ifs.somtoolbox.models;

