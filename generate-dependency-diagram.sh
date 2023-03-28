#
# Copyright 2015-2023 Ritense BV, the Netherlands.
#
# Licensed under EUPL, Version 1.2 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" basis,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#/bin/bash


rm module-dependencies.dot
rm output.pdf

./gradlew generateModulesGraphvizText  --no-configure-on-demand -Pmodules.graph.output.gv=module-dependencies.dot
until [ -f module-dependencies.dot ]
do
     sleep 1
done
gsed -i 's/ \[color=red style=bold\]//g' module-dependencies.dot
gsed -i '/^":app:.*$/d' module-dependencies.dot
gsed -i '/^":test-utils-common.*$/d' module-dependencies.dot
gsed -i '/^":.*-dependencies.*$/d' module-dependencies.dot
#gsed -i '/^$/d' module-dependencies.dot
dot -Kneato -Tsvg module-dependencies.dot -Gsplines="ortho" -Gconcentrate -Gnormalize -Goverlap="vpsc" -Gesep=+20 -Earrowsize=0.5 -Nshape=record -Nstyle="filled" -Nfillcolor="#f2f2f2" > output.svg