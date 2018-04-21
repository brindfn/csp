# check if name and domain are set
: ${DOMAIN?Need a value for DOMAIN}

# build domain and fqdn vars
XXXXMPPDOMAINXXX=teleconf.${DOMAIN}
XXXXMPPFQDNXXX=teleconf.${DOMAIN}

# check if postgres vars are set
: ${POSTGRES_DOCR_PORT?Need a value for POSTGRES_DOCR_PORT}
: ${POSTGRES_USER?Need a value for POSTGRES_USER}
: ${POSTGRES_PASSWORD?Need a value for POSTGRES_PASSWORD}

# build database vars
XXXDBHOSTXXX=${XXXDBHOSTXXX-localhost}
XXXDBPORTXXX=${POSTGRES_DOCR_PORT}
XXXDBUSERNAMEXXX=${POSTGRES_USER}
XXXDBPASSWORDXXX=${POSTGRES_PASSWORD}

# secure port (insecure port - default 9090 - is disabled in openfire.xml by setting value to -1)
XXXADMINCONSOLESECUREPORTXXX=${XXXADMINCONSOLESECUREPORTXXX-9091}

# other variables
XXXFOCUSUSERJIDXXX=${XXXFOCUSUSERJIDXXX-focus@csp}
XXXFOCUSPASSXXX=${XXXFOCUSPASSXXX-focuspas}
XXXRESTAPIENABLEDXXX=${XXXRESTAPIENABLEDXXX-true}
XXXMEDIAMINPORTXXX=${XXXMEDIAMINPORTXXX-5000}
XXXMEDIAMAXPORTXXX=${XXXMEDIAMAXPORTXXX-5020}
XXXVIDEOSTRCRYPTOCONTEXTCHECKREPLAYXXX=${XXXVIDEOSTRCRYPTOCONTEXTCHECKREPLAYXXX-true}

default_iface=$(awk '$2 == 00000000 { print $1 }' /proc/net/route);
XXXNATLOCALADDRESSXXX=$(ip addr show dev "$default_iface" | awk '$1 ~ /^inet/ { sub("/.*", "", $2); print $2 }' | head -1)
XXXNATPUBLICADDRESSXXX=$( curl http://ifconfig.me );

XXXOFMEETAUDIOMIXERXXX=${XXXOFMEETAUDIOMIXERXXX-true}
XXXOFMEETRECORDINGSECRETXXX=${XXXOFMEETRECORDINGSECRETXXX-secret}
XXXOFMEETRECORDINGPATHXXX=${XXXOFMEETRECORDINGPATHXXX-/usr/share/openfire/resources/spank/ofmeet-cdn/recordings}
XXXOFMEETADAPTIVELASTNXXX=${XXXOFMEETADAPTIVELASTNXXX-true}
XXXOFMEETADAPTIVESIMULCASTXXX=${XXXOFMEETADAPTIVESIMULCASTXXX-true}
XXXOFMEETENABLESIMULCASTXXX=${XXXOFMEETENABLESIMULCASTXXX-true}
XXXADMINPASSXXX=${XXXADMINPASSXXX-adminpas}
XXXADMINEMAILXXX=${XXXADMINEMAILXXX-admin@csp.com}
XXXOWNHOSTXXX=${XXXXMPPFQDNXXX}

sed -i "s/XXXXMPPDOMAINXXX/${XXXXMPPDOMAINXXX}/g" "${1}"
sed -i "s/XXXXMPPFQDNXXX/${XXXXMPPFQDNXXX}/g" "${1}"
sed -i "s/XXXADMINCONSOLEPORTXXX/${XXXADMINCONSOLEPORTXXX}/g" "${1}"
sed -i "s/XXXADMINCONSOLESECUREPORTXXX/${XXXADMINCONSOLESECUREPORTXXX}/g" "${1}"
sed -i "s/XXXDBHOSTXXX/${XXXDBHOSTXXX}/g" "${1}"
sed -i "s/XXXDBPORTXXX/${XXXDBPORTXXX}/g" "${1}"
sed -i "s/XXXDBUSERNAMEXXX/${XXXDBUSERNAMEXXX}/g" "${1}"
sed -i "s/XXXDBPASSWORDXXX/${XXXDBPASSWORDXXX}/g" "${1}"
sed -i "s/XXXFOCUSUSERJIDXXX/${XXXFOCUSUSERJIDXXX}/g" "${1}"
sed -i "s/XXXFOCUSPASSXXX/${XXXFOCUSPASSXXX}/g" "${1}"
sed -i "s/XXXRESTAPIENABLEDXXX/${XXXRESTAPIENABLEDXXX}/g" "${1}"
sed -i "s/XXXMEDIAMINPORTXXX/${XXXMEDIAMINPORTXXX}/g" "${1}"
sed -i "s/XXXMEDIAMAXPORTXXX/${XXXMEDIAMAXPORTXXX}/g" "${1}"
sed -i "s/XXXVIDEOSTRCRYPTOCONTEXTCHECKREPLAYXXX/${XXXVIDEOSTRCRYPTOCONTEXTCHECKREPLAYXXX}/g" "${1}"
sed -i "s/XXXNATLOCALADDRESSXXX/${XXXNATLOCALADDRESSXXX}/g" "${1}"
sed -i "s/XXXNATPUBLICADDRESSXXX/${XXXNATPUBLICADDRESSXXX}/g" "${1}"
sed -i "s/XXXOFMEETAUDIOMIXERXXX/${XXXOFMEETAUDIOMIXERXXX}/g" "${1}"
sed -i "s/XXXOFMEETRECORDINGSECRETXXX/${XXXOFMEETRECORDINGSECRETXXX}/g" "${1}"
sed -i "s#XXXOFMEETRECORDINGPATHXXX#${XXXOFMEETRECORDINGPATHXXX}#g" "${1}"
sed -i "s/XXXOFMEETADAPTIVELASTNXXX/${XXXOFMEETADAPTIVELASTNXXX}/g" "${1}"
sed -i "s/XXXOFMEETADAPTIVESIMULCASTXXX/${XXXOFMEETADAPTIVESIMULCASTXXX}/g" "${1}"
sed -i "s/XXXOFMEETENABLESIMULCASTXXX/${XXXOFMEETENABLESIMULCASTXXX}/g" "${1}"
sed -i "s/XXXADMINPASSXXX/${XXXADMINPASSXXX}/g" "${1}"
sed -i "s/XXXADMINEMAILXXX/${XXXADMINEMAILXXX}/g" "${1}"
sed -i "s/XXXOWNHOSTXXX/${XXXOWNHOSTXXX}/g" "${1}"
